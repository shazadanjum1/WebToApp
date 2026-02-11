package com.app.styletap.webtoappconverter.presentations.ui.activities.Premium

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityLifeTimePremiumBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.animateViewXaxis
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.changeToDeviceLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge2
import com.app.styletap.webtoappconverter.extentions.extractNumericValue
import com.app.styletap.webtoappconverter.presentations.utils.Contants.LIFETIME_PRICE
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency

class LifeTimePremiumActivity : AppCompatActivity() {
    lateinit var binding: ActivityLifeTimePremiumBinding

    private val PREMIUM_LIFETIME_PACKAGE = "bundle_download"

    private var mLifeTimePlanIndex = 0

    private var billingClient: BillingClient? = null
    private var productInAppDetailsList = mutableListOf<ProductDetails>()
    private val inAppProductIds = arrayListOf(PREMIUM_LIFETIME_PACKAGE)

    var fromWhere = "splash"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //changeLocale()
        changeToDeviceLocale()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityLifeTimePremiumBinding.inflate(layoutInflater)
        setContentView(binding.root)
        /*customEnableEdgeToEdge()

        adjustTopHeight(binding.toolbarLL)
        adjustBottomHeight(binding.container)*/

        customEnableEdgeToEdge2()


        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    //onBack()
                }
            })

        binding.buySubscriptionBtn.animateViewXaxis()

        setUpView()

    }

    private fun setUpView() {
        intent?.extras?.let {
            fromWhere = it.getString("from","splash")
        }

        /*val lifetimePrice = sharedPreferencePremium?.getString(LIFETIME_PRICE) ?: "*****"
        binding.priceTv.text = lifetimePrice*/

        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        purchase?.let { verifyPurchase(it) }
                    }
                }
            }.build()

        establishConnection()

        binding.backBtn.setOnClickListener {
            onBack()
        }

        binding.buySubscriptionBtn.setOnClickListener {
            handlePurchase()
        }

    }

    fun onBack(){
        finish()
    }


    private fun handlePurchase() {
        try {
            productInAppDetailsList.let {
                launchBillingFlow(it[mLifeTimePlanIndex], BillingClient.ProductType.INAPP)
            }
        } catch (_: Exception) {}
    }

    private fun establishConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    showInAppProducts()
                }
                try {
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.progressBar.visibility = View.GONE
                    }
                } catch (_: Exception) {}
            }

            override fun onBillingServiceDisconnected() {
                try {
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.progressBar.visibility = View.GONE
                    }
                } catch (_: Exception) {}
            }
        })
    }

    private fun verifyPurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val purchasedProduct = purchase.products[0]
                if (purchasedProduct == PREMIUM_LIFETIME_PACKAGE) {
                    PrefHelper.setIsPurchasedLifeTime(true)
                    /*val mIntent = Intent(this@LifeTimePremiumActivity, MainActivity::class.java)
                    startActivity(mIntent)
                    finishAffinity()*/
                    finish()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showInAppProducts() {
        val inAppProducts = inAppProductIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(inAppProducts)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, prodDetailsList ->
            if (prodDetailsList.productDetailsList.isNotEmpty()) {
                productInAppDetailsList.clear()
                Handler(Looper.getMainLooper()).postDelayed({
                    productInAppDetailsList.addAll(prodDetailsList.productDetailsList)
                    updatePricesInView(productInAppDetailsList)
                }, 20)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updatePricesInView(detailsList: MutableList<ProductDetails>) {
        try {

            var i = 0

            for (product in detailsList) {
                if (product.productId == PREMIUM_LIFETIME_PACKAGE) {
                    mLifeTimePlanIndex = i
                    val lifetimePrice = product.oneTimePurchaseOfferDetails?.formattedPrice
                    binding.priceTv.text = lifetimePrice

                    if (lifetimePrice != null) {

                        // Remove currency symbol and convert to Double
                        val price30Percent = extractNumericValue(lifetimePrice)

                        // Calculate original (100%)
                        val originalPrice = price30Percent / 0.3

                        // Calculate 70%
                        val seventyPercent = originalPrice * 0.7

                        val currencyCode = product.oneTimePurchaseOfferDetails?.priceCurrencyCode ?: "USD"
                        val currency = Currency.getInstance(currencyCode)


                        val formatter = NumberFormat.getCurrencyInstance() as DecimalFormat
                        val symbols = formatter.decimalFormatSymbols

                        formatter.decimalFormatSymbols = symbols
                        formatter.currency = currency


                        // Strike-through original price
                        val spannable = SpannableString(formatter.format(originalPrice))
                        spannable.setSpan(
                            StrikethroughSpan(),
                            0, spannable.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        binding.originalPriceTv.text = spannable

                        binding.percentageTv.text = getString(
                            R.string.save_amount_today_only,
                            formatter.format(seventyPercent)
                        )

                        PrefHelper.setString(LIFETIME_PRICE, lifetimePrice)
                    }
                }
                i++
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun launchBillingFlow(productDetails: ProductDetails, productType: String) {
        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)

        if (productType == BillingClient.ProductType.SUBS) {
            val offerToken = productDetails.subscriptionOfferDetails?.get(0)?.offerToken.orEmpty()
            productDetailsParamsBuilder.setOfferToken(offerToken)
        } else if (productType == BillingClient.ProductType.INAPP) {
            // No offer token needed for one-time purchases
        }

        val productDetailsParams = productDetailsParamsBuilder.build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient?.launchBillingFlow(this, billingFlowParams)
    }
}