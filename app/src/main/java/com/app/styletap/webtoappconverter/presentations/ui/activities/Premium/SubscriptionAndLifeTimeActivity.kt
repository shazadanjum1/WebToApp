package com.app.styletap.webtoappconverter.presentations.ui.activities.Premium

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StrikethroughSpan
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.app.styletap.webtoappconverter.databinding.ActivitySubscriptionAndLifeTimeBinding
import com.app.styletap.webtoappconverter.extentions.calculateMonthlyPriceFromFormattedPrice
import com.app.styletap.webtoappconverter.extentions.changeToDeviceLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge2
import com.app.styletap.webtoappconverter.extentions.openLink
import com.app.styletap.webtoappconverter.presentations.ui.activities.authorization.LoginActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.home.MainActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.language.LanguageActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.onboarding.OnboardingActivity
import com.app.styletap.webtoappconverter.presentations.utils.Contants.isLanguageSelected
import com.app.styletap.webtoappconverter.presentations.utils.Contants.isShowOnBoarding
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency

class SubscriptionAndLifeTimeActivity : AppCompatActivity() {
    lateinit var binding: ActivitySubscriptionAndLifeTimeBinding
    var fromWhere = "splash"
    private lateinit var auth: FirebaseAuth
    var user: FirebaseUser? = null
    private var handler: Handler? = null

    private val PREMIUM_LIFETIME_PACKAGE = "premium_lifetime"

    private val PREMIUM_WEEKLY_PACKAGE = "weekly_sub"
    private val PREMIUM_YEARLY_PACKAGE = "yearly_sub"

    private var weeklyPlanIndex = 0
    private var yearlyPlanIndex = 1
    private var lifeTimePlanIndex = 5//2
    private var mLifeTimePlanIndex = 0

    private var checkClickedPlan = 1
    private var billingClient: BillingClient? = null
    private var productDetailsList = mutableListOf<ProductDetails>()
    private var productInAppDetailsList = mutableListOf<ProductDetails>()
    private val subscriptionProductIds = arrayListOf(PREMIUM_YEARLY_PACKAGE, PREMIUM_WEEKLY_PACKAGE)
    private val inAppProductIds = arrayListOf(PREMIUM_LIFETIME_PACKAGE)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeToDeviceLocale()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivitySubscriptionAndLifeTimeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        /*customEnableEdgeToEdge()

        adjustTopHeight(binding.toolbarLL)
        adjustBottomHeight(binding.container)*/

        customEnableEdgeToEdge2()


        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    //onBack()
                }
            })

        handler = Handler(Looper.getMainLooper())


        setUpView()
        setupTermsText()
    }

    fun onBack() {
        if (fromWhere == "splash"){
            fromSplash()
        } else {
            finish()
        }
    }

    fun fromSplash(){
        moveNext()
        /*if (PrefHelper.getIsPurchased() || !PrefHelper.getBooleanDefultTrue(splash_inter)){
            moveNext()
        } else {
            InterstitialAdManager(this).loadAndShowAd(
                getString(R.string.splashInterstitialId),
                PrefHelper.getBooleanDefultTrue(splash_inter),
                object : InterstitialLoadCallback{
                    override fun onFailedToLoad() {
                        Toast.makeText(this@SubscriptionPremiumActivity, resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                        moveNext()
                    }
                    override fun onLoaded() {
                        moveNext()
                    }
                }
            )
        }*/
    }

    fun moveNext(){
        val mIntent = if (user?.isAnonymous == true) {
            Intent(this, MainActivity::class.java)
        } else if (user == null) {
            if (!PrefHelper.getBoolean(isLanguageSelected)){
                Intent(this, LanguageActivity::class.java)
            } else if (PrefHelper.getBooleanDefultTrue(isShowOnBoarding)){
                Intent(this, OnboardingActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
        } else {
            Intent(this, MainActivity::class.java)
        }
        mIntent.apply {
            putExtra("from", "splash")
        }
        startActivity(mIntent)
        finish()
    }

    fun setUpView(){
        intent?.extras?.let{
            fromWhere = it.getString("from", "home")
        }

        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .enablePrepaidPlans()
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

        binding.proYearlyBtn.setOnClickListener {
            checkClickedPlan = yearlyPlanIndex
            handlePurchase()
        }

        binding.proWeeklyBtn.setOnClickListener {
            checkClickedPlan = weeklyPlanIndex
            handlePurchase()
        }

        binding.proLifeTimeBtn.setOnClickListener {
            checkClickedPlan = lifeTimePlanIndex
            handlePurchase()
        }

    }

    private fun handlePurchase() {
        try {
            if (checkClickedPlan == yearlyPlanIndex || checkClickedPlan == weeklyPlanIndex){
                productDetailsList.let {
                    when (checkClickedPlan) {
                        yearlyPlanIndex -> {
                            launchBillingFlow(it[checkClickedPlan], BillingClient.ProductType.SUBS)
                        }
                        weeklyPlanIndex -> {
                            launchBillingFlow(it[checkClickedPlan], BillingClient.ProductType.SUBS)
                        }
                    }
                }
            } else if (checkClickedPlan == lifeTimePlanIndex){
                productInAppDetailsList.let {
                    launchBillingFlow(it[mLifeTimePlanIndex], BillingClient.ProductType.INAPP)
                }
            }

        } catch (_: Exception) {}
    }

    private fun establishConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    showSubscriptionProducts()
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
                if (purchasedProduct in subscriptionProductIds || purchasedProduct == PREMIUM_LIFETIME_PACKAGE) {
                    PrefHelper.setIsPurchased(true)


                    try {
                        if (!isFinishing){
                            if (fromWhere == "splash"){
                                moveNext()
                            } else {
                                val mIntent = Intent(this@SubscriptionAndLifeTimeActivity, MainActivity::class.java)
                                startActivity(mIntent)
                                finishAffinity()
                            }

                        }
                    }catch (_: Exception){}
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showSubscriptionProducts() {
        val subscriptionProducts = subscriptionProductIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(subscriptionProducts)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, prodDetailsList ->
            if (prodDetailsList.productDetailsList.isNotEmpty()) {
                productDetailsList.clear()
                Handler(Looper.getMainLooper()).postDelayed({
                    productDetailsList.addAll(prodDetailsList.productDetailsList)
                    updatePricesInView(productDetailsList)
                }, 20)
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

    @SuppressLint("SetTextI18n")
    private fun updatePricesInView(detailsList: MutableList<ProductDetails>) {
        try {

            var i = 0
            var _YearlyPrice = "0.0"
            var _WeeklyPrice = "0.0"

            for (product in detailsList) {
                if (product.productId == PREMIUM_WEEKLY_PACKAGE) {
                    weeklyPlanIndex = i
                    //val weeklyPrice = product.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice

                    val weeklyPrice = try {
                        product.subscriptionOfferDetails
                            ?.get(0)?.pricingPhases?.pricingPhaseList?.get(1)?.formattedPrice
                    } catch (_: Exception) {
                        product.subscriptionOfferDetails
                            ?.get(0)?.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice
                    }


                    val formattedPrice = weeklyPrice?.toDoubleOrNull()?.let {
                        String.format("%.2f", it)
                    } ?: weeklyPrice

                    binding.weeklyPriceTv.text = getString(R.string.try_3_days_for_free_then_week, formattedPrice)


                    if (weeklyPrice != null) {
                        _WeeklyPrice = weeklyPrice
                    }

                } else if (product.productId == PREMIUM_YEARLY_PACKAGE) {
                    yearlyPlanIndex = i
                    checkClickedPlan = yearlyPlanIndex

                    val yearlyPrice = try {
                        product.subscriptionOfferDetails
                            ?.get(0)?.pricingPhases?.pricingPhaseList?.get(1)?.formattedPrice
                    } catch (_: Exception) {
                        product.subscriptionOfferDetails
                            ?.get(0)?.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice
                    }

                    val formattedPrice = yearlyPrice?.toDoubleOrNull()?.let {
                        String.format("%.2f", it)
                    } ?: yearlyPrice

                    binding.yearlyPriceTv.text = getString(R.string.just_per_year, formattedPrice)

                    val yearlyPriceDouble = yearlyPrice?.let { priceStr ->
                        val numeric = priceStr.replace("[^0-9.]".toRegex(), "")
                        numeric.toDoubleOrNull()
                    }
                    val weeklyPrice = yearlyPriceDouble?.let { it / 52 }
                    val currencyUnit = yearlyPrice?.replace("[0-9.,\\s]".toRegex(), "") ?: ""
                    val formattedWeeklyPrice = weeklyPrice?.let { "$currencyUnit ${String.format("%.0f", it)}" } ?: "$currencyUnit 0"
                    binding.perWeekTv.text = formattedWeeklyPrice


                    if (yearlyPrice != null) {
                        _YearlyPrice = yearlyPrice
                        //applyYearlyOff( _YearlyPrice, product)
                    }

                } else if (product.productId == PREMIUM_LIFETIME_PACKAGE) {
                    mLifeTimePlanIndex = i
                    val lifetimePrice = product.oneTimePurchaseOfferDetails?.formattedPrice
                    binding.lifetimePriceTv.text = lifetimePrice

                }
                i++
            }



        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    fun applyYearlyOff(_YearlyPrice: String, product: ProductDetails){
        try {

            val wPricePerMonthFromYearly = calculateMonthlyPriceFromFormattedPrice(_YearlyPrice)
            //binding.perMonthTv.text = "${String.format("%.2f", wPricePerMonthFromYearly)}"

            val currencyCode = product.oneTimePurchaseOfferDetails?.priceCurrencyCode ?: "USD"
            val currency = Currency.getInstance(currencyCode)


            val formatter = NumberFormat.getCurrencyInstance() as DecimalFormat
            val symbols = formatter.decimalFormatSymbols

            formatter.decimalFormatSymbols = symbols
            formatter.currency = currency


            // Strike-through original price
            val spannable = SpannableString(formatter.format(wPricePerMonthFromYearly))
            spannable.setSpan(
                StrikethroughSpan(),
                0, spannable.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.perWeekTv.text = spannable

        }catch (_: Exception){}
    }


    private fun setupTermsText() {

        val terms = getString(R.string.terms_of_use)
        val privacy = getString(R.string.privacy_policy)
        val cancel = getString(R.string.cancel_anytime)

        val fullText = getString(
            R.string.terms_of_use_privacy_policy_cancel_anytime,
            terms,
            privacy,
            cancel
        )

        val spannable = SpannableString(fullText)

        val textColor = ContextCompat.getColor(this, R.color.pro_c7)

        fun setClickable(text: String, action: () -> Unit) {
            val start = fullText.indexOf(text)
            val end = start + text.length

            if (start >= 0) {
                spannable.setSpan(object : ClickableSpan() {

                    override fun onClick(widget: View) {
                        action()
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = textColor          // 👈 custom color
                        ds.isUnderlineText = false    // remove underline (optional)
                    }

                }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        setClickable(terms) {
            openLink(resources.getString(R.string.privacy_policy_link))
        }

        setClickable(privacy) {
            openLink(resources.getString(R.string.privacy_policy_link))
        }

        setClickable(cancel) {
            openSubscriptions()
        }

        binding.termsAndConditionTv.text = spannable
        binding.termsAndConditionTv.movementMethod = LinkMovementMethod.getInstance()
        binding.termsAndConditionTv.highlightColor = Color.TRANSPARENT
    }

    private fun openSubscriptions() {
        val packageName = packageName
        val uri = Uri.parse("https://play.google.com/store/account/subscriptions?package=$packageName")

        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }


}