package com.app.styletap.webtoappconverter.presentations.ui.activities.Premium

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.app.styletap.ads.InterstitialAdManager
import com.app.styletap.interfaces.InterstitialLoadCallback
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivitySubscriptionPremiumBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.animateViewXaxis
import com.app.styletap.webtoappconverter.extentions.calculateMonthlyPriceFromFormattedPrice
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.changeToDeviceLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge2
import com.app.styletap.webtoappconverter.extentions.dpToPx
import com.app.styletap.webtoappconverter.extentions.extractNumericValue
import com.app.styletap.webtoappconverter.presentations.ui.activities.authorization.LoginActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.home.MainActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.language.LanguageActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.onboarding.OnboardingActivity
import com.app.styletap.webtoappconverter.presentations.utils.Contants.WEEKLY_PRICE
import com.app.styletap.webtoappconverter.presentations.utils.Contants.YEARLY_PRICE
import com.app.styletap.webtoappconverter.presentations.utils.Contants.isLanguageSelected
import com.app.styletap.webtoappconverter.presentations.utils.Contants.isShowOnBoarding
import com.app.styletap.webtoappconverter.presentations.utils.Contants.is_show_onboarding_screen
import com.app.styletap.webtoappconverter.presentations.utils.Contants.splash_inter
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.jvm.java

class SubscriptionPremiumActivity : AppCompatActivity() {
    lateinit var binding: ActivitySubscriptionPremiumBinding

    private val PREMIUM_WEEKLY_PACKAGE = "weekly_sub"
    private val PREMIUM_YEARLY_PACKAGE = "yearly_sub"

    private var weeklyPlanIndex = 0
    private var yearlyPlanIndex = 1


    private var checkClickedPlan = 1
    private var billingClient: BillingClient? = null
    private var productDetailsList = mutableListOf<ProductDetails>()
    private val productIdsSubscription = arrayListOf(PREMIUM_YEARLY_PACKAGE, PREMIUM_WEEKLY_PACKAGE)
    private var handler: Handler? = null

    var fromWhere = "home"

    private lateinit var auth: FirebaseAuth
    var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeToDeviceLocale()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivitySubscriptionPremiumBinding.inflate(layoutInflater)
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

        binding.buySubscriptionBtn.animateViewXaxis()

        setUpView()

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
            } else if (PrefHelper.getBooleanDefultTrue(isShowOnBoarding) && PrefHelper.getBooleanDefultTrue(is_show_onboarding_screen)){
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

        /*val yearlyPrice = sharedPreferencePremium.getString(YEARLY_PRICE) ?: "*****"
        val weeklyPrice = sharedPreferencePremium.getString(WEEKLY_PRICE) ?: "*****"

        binding.weeklyPriceTv.text = weeklyPrice
        binding.yearlyPriceTv.text = yearlyPrice

        applyYearlyOff(yearlyPrice)*/

        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .enablePrepaidPlans()
                    .build()
            )
            .setListener { billingResult, list ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
                    for (purchase in list) {
                        purchase?.let { verifySubPurchase(it) }
                    }
                }
            }.build()


        establishConnection()

        binding.backBtn.setOnClickListener {
            onBack()
        }

        binding.countinueWithAdBtn.setOnClickListener {

            if (fromWhere == "splash"){
                moveNext()
            } else {
                finish()
            }

            /*if (PrefHelper.getIsPurchased() || !PrefHelper.getBooleanDefultTrue(splash_inter)){
                if (fromWhere == "splash"){
                    moveNext()
                } else {
                    finish()
                }
            } else {
                InterstitialAdManager(this).loadAndShowAd(
                    getString(R.string.splashInterstitialId),
                    PrefHelper.getBooleanDefultTrue(splash_inter),
                    object : InterstitialLoadCallback{
                        override fun onFailedToLoad() {
                            Toast.makeText(this@SubscriptionPremiumActivity, resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                            if (fromWhere == "splash"){
                                moveNext()
                            } else {
                                finish()
                            }
                        }
                        override fun onLoaded() {
                            if (fromWhere == "splash"){
                                moveNext()
                            } else {
                                finish()
                            }
                        }
                    }
                )
            }*/



        }

        binding.yearlyCrd.setOnClickListener {
            checkClickedPlan = yearlyPlanIndex
            updateUIForSelectedPlan(1)
        }

        binding.weeklyCrd.setOnClickListener {
            checkClickedPlan = weeklyPlanIndex
            updateUIForSelectedPlan(0)
        }

        binding.buySubscriptionBtn.setOnClickListener {
            handleSubscriptionPurchase()
        }

    }

    private fun updateUIForSelectedPlan(plan: Int) {
        binding.apply {
            weeklyProCheckIv.isVisible = plan == 0
            proCheckIv.isVisible = plan == 1

            if (plan == 0){
                weeklyCrd.strokeColor = ContextCompat.getColor(this@SubscriptionPremiumActivity, R.color.blue)
                yearlyCrd.strokeColor = ContextCompat.getColor(this@SubscriptionPremiumActivity, R.color.pro_c6)

                weeklyCrd.strokeWidth = 2.dpToPx(this@SubscriptionPremiumActivity)
                yearlyCrd.strokeWidth = 1.dpToPx(this@SubscriptionPremiumActivity)

                weeklyCL.background = ContextCompat.getDrawable(this@SubscriptionPremiumActivity, R.drawable.pro_container1)
                yearlyCL.setBackgroundColor(ContextCompat.getColor(this@SubscriptionPremiumActivity, R.color.pro_c6))
            } else if (plan == 1) {
                weeklyCrd.strokeColor = ContextCompat.getColor(this@SubscriptionPremiumActivity, R.color.pro_c6)
                yearlyCrd.strokeColor = ContextCompat.getColor(this@SubscriptionPremiumActivity, R.color.blue)

                weeklyCrd.strokeWidth = 1.dpToPx(this@SubscriptionPremiumActivity)
                yearlyCrd.strokeWidth = 2.dpToPx(this@SubscriptionPremiumActivity)

                weeklyCL.setBackgroundColor(ContextCompat.getColor(this@SubscriptionPremiumActivity, R.color.pro_c6))
                yearlyCL.background = ContextCompat.getDrawable(this@SubscriptionPremiumActivity, R.drawable.pro_container1)
            }

        }
/*
        binding.ayearBtn.setBackgroundResource(if (plan == 2) R.drawable.ic_premium_btn_selected else R.drawable.ic_premium_btn_unselected)
        binding.monthBtn.setBackgroundResource(if (plan == 0) R.drawable.ic_premium_btn_selected else R.drawable.ic_premium_btn_unselected)
        binding.weeklyBtm.setBackgroundResource(if (plan == 1) R.drawable.ic_premium_btn_selected else R.drawable.ic_premium_btn_unselected)
    */
    }

    private fun handleSubscriptionPurchase() {
        try {
            productDetailsList.let {
                when (checkClickedPlan) {
                    yearlyPlanIndex -> {
                        launchSubscriptionFlow(it[checkClickedPlan])
                    }
                    weeklyPlanIndex -> {
                        launchSubscriptionFlow(it[checkClickedPlan])
                    }
                }
            }
        }catch (_: Exception){}
    }

    private fun establishConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    showSubscriptionProducts()
                }
                try {
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.progressBar.visibility = View.GONE
                    }
                }catch (_: Exception){}
            }

            override fun onBillingServiceDisconnected() {
                try {
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.progressBar.visibility = View.GONE
                    }
                }catch (_: Exception){}
            }
        })
    }


    private fun verifySubPurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val purchasedProduct = purchase.products[0]
                if (purchasedProduct in productIdsSubscription) {
                    PrefHelper.setIsPurchased(true)
                    //isProUser = true
                    //finish()
                    try {
                        if (!isFinishing){
                            if (fromWhere == "splash"){
                                moveNext()
                            } else {
                                val mIntent = Intent(this@SubscriptionPremiumActivity, MainActivity::class.java)
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
        val productList = productIdsSubscription.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, prodDetailsList ->
            if (prodDetailsList.productDetailsList.isNotEmpty()) {
                productDetailsList.clear()
                handler?.postDelayed({
                    productDetailsList.addAll(prodDetailsList.productDetailsList)
                    updatePricesInView()
                }, 20)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updatePricesInView() {
        try {
            var i = 0

            var _YearlyPrice = "0.0"
            var _WeeklyPrice = "0.0"

            for (product in productDetailsList){
                Log.d("productDetails", "${i} : ${product.productId}")
                if (product.productId == PREMIUM_WEEKLY_PACKAGE){
                    weeklyPlanIndex = i
                    checkClickedPlan = weeklyPlanIndex

                    val weeklyPrice = product.subscriptionOfferDetails
                        ?.get(0)?.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice
                    binding.weeklyPriceTv.text = weeklyPrice

                    if (weeklyPrice != null) {
                        _WeeklyPrice = weeklyPrice
                        PrefHelper.setString(WEEKLY_PRICE, weeklyPrice)
                    }

                }else if (product.productId == PREMIUM_YEARLY_PACKAGE){
                    yearlyPlanIndex = i

                    val yearlyPrice = try {
                        product.subscriptionOfferDetails
                            ?.get(0)?.pricingPhases?.pricingPhaseList?.get(1)?.formattedPrice

                    } catch (_: Exception){
                        product.subscriptionOfferDetails
                            ?.get(0)?.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice
                    }

                    binding.yearlyPriceTv.text = "$yearlyPrice"

                    /*binding.yearlyMessageTv.text =
                        "${resources.getString(R.string._3_day_free_trial_then)} $yearlyPrice /${
                            resources.getString(R.string.year)
                        }"*/

                    if (yearlyPrice != null) {
                        _YearlyPrice = yearlyPrice

                        PrefHelper.setString(YEARLY_PRICE, yearlyPrice)
                    }

                }
                i++
            }


            applyYearlyOff(_WeeklyPrice, _YearlyPrice)


        } catch (_: Exception) { }

    }

    @SuppressLint("SetTextI18n")
    fun applyYearlyOff(_WeeklyPrice: String, _YearlyPrice: String){
        try {
            val wPricePerMonthFromYearly = calculateMonthlyPriceFromFormattedPrice(_YearlyPrice)
            binding.perMonthTv.text = "${String.format("%.2f", wPricePerMonthFromYearly)} ${resources.getString(R.string.per_week)}"

            val weeklyPrice = extractNumericValue(_WeeklyPrice)
            val yearlyPrice = extractNumericValue(_YearlyPrice)

            val yearlyIfWeekly = weeklyPrice * 52
            val discountAmount = yearlyIfWeekly - yearlyPrice
            val discountPercentage = (discountAmount / yearlyIfWeekly) * 100

            val discountText = "${resources.getString(R.string.save)} ${discountPercentage.toInt()}%"
            binding.percentageTv.text = discountText

        }catch (_: Exception){}
    }

    private fun launchSubscriptionFlow(productDetails: ProductDetails) {
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(productDetails.subscriptionOfferDetails?.get(0)?.offerToken.orEmpty())
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient?.launchBillingFlow(this, billingFlowParams)
    }
}