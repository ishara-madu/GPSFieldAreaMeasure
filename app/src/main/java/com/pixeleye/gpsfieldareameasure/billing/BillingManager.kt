package com.pixeleye.gpsfieldareameasure.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.pixeleye.gpsfieldareameasure.model.VipPackage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BillingManager(private val context: Context) {

    private val _vipPackages = MutableStateFlow<List<VipPackage>>(emptyList())
    val vipPackages = _vipPackages.asStateFlow()

    // Default fallback packages if billing fails or no products found
    private val fallbackPackages = listOf(
        VipPackage("weekly", "Silver", "$0.25", "Weekly", listOf("Remove Ads", "Cloud Backup"), false),
        VipPackage("monthly", "Gold", "$0.75", "Monthly", listOf("Remove Ads", "Cloud Backup", "Unlimited Points"), true),
        VipPackage("yearly", "Platinum", "$3.00", "Yearly", listOf("All Features", "Priority Support"), false)
    )

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            // Handle success
            // In a real app, verify signature and grant entitlement
        }
    }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    init {
        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    querySkuDetails()
                } else {
                    // Fallback on error
                    _vipPackages.update { fallbackPackages }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart connection later
                // For now, use fallback
                _vipPackages.update { fallbackPackages }
            }
        })
    }

    private fun querySkuDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("vip_weekly")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("vip_monthly")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("vip_yearly")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val mappedPackages = productDetailsList.map { productDetails ->
                    val offer = productDetails.subscriptionOfferDetails?.firstOrNull()
                    val price = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "N/A"
                    val durationCode = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.billingPeriod ?: ""
                    
                    // Simple logic to determine duration name from period code (P1W, P1M, P1Y)
                    val durationName = when {
                        durationCode.contains("W") -> "Weekly"
                        durationCode.contains("M") -> "Monthly"
                        durationCode.contains("Y") -> "Yearly"
                        else -> "Subscription"
                    }

                    val name = when {
                        productDetails.productId.contains("weekly") -> "Silver"
                        productDetails.productId.contains("monthly") -> "Gold"
                        productDetails.productId.contains("yearly") -> "Platinum"
                        else -> productDetails.name
                    }
                    
                    val features = when {
                         productDetails.productId.contains("weekly") -> listOf("Remove Ads", "Cloud Backup")
                         productDetails.productId.contains("monthly") -> listOf("Remove Ads", "Cloud Backup", "Unlimited Points")
                         productDetails.productId.contains("yearly") -> listOf("All Features", "Priority Support")
                         else -> listOf("Premium Features")
                    }

                    VipPackage(
                        id = productDetails.productId,
                        name = name,
                        price = price,
                        duration = durationName,
                        features = features,
                        isBestValue = productDetails.productId.contains("monthly")
                    )
                }
                
                // Sort to match desired order: Weekly, Monthly, Yearly
                val sortedPackages = mappedPackages.sortedBy { 
                    when(it.id) {
                        "vip_weekly" -> 1
                        "vip_monthly" -> 2
                        "vip_yearly" -> 3
                        else -> 4
                    }
                }
                
                _vipPackages.update { sortedPackages }
            } else {
                // Fallback if no products found (e.g. not configured in Play Console yet)
                _vipPackages.update { fallbackPackages }
            }
        }
    }
}
