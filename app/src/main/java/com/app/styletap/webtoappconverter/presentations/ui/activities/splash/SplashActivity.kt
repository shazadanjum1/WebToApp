package com.app.styletap.webtoappconverter.presentations.ui.activities.splash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.app.styletap.ads.ConsentManager
import com.app.styletap.ads.InterstitialAdManager
import com.app.styletap.interfaces.InterstitialLoadCallback
import com.app.styletap.interfaces.RemoteConfigCallbackListiner
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge2
import com.app.styletap.webtoappconverter.extentions.isNetworkAvailable
import com.app.styletap.webtoappconverter.extentions.proIntent
import com.app.styletap.webtoappconverter.firebase.RemoteConfigHelper
import com.app.styletap.webtoappconverter.presentations.ui.activities.authorization.LoginActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.home.MainActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.language.LanguageActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.onboarding.OnboardingActivity
import com.app.styletap.webtoappconverter.presentations.utils.Contants.isIntertialAdshowing
import com.app.styletap.webtoappconverter.presentations.utils.Contants.isLanguageSelected
import com.app.styletap.webtoappconverter.presentations.utils.Contants.isShowOnBoarding
import com.app.styletap.webtoappconverter.presentations.utils.Contants.is_show_iap_screen
import com.app.styletap.webtoappconverter.presentations.utils.Contants.is_show_onboarding_screen
import com.app.styletap.webtoappconverter.presentations.utils.Contants.splash_inter
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SplashActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    var user: FirebaseUser? = null

    private val PREMIUM_LIFETIME_PACKAGE = "bundle_download"
    private val PREMIUM_LIFETIME_PACKAGE_FOR_ADS = "premium_lifetime"


    private lateinit var googleMobileAdsConsentManager: ConsentManager

    private var billingClient: BillingClient? = null

    private val PREMIUM_WEEKLY_PACKAGE = "weekly_sub"
    private val PREMIUM_YEARLY_PACKAGE = "yearly_sub"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeLocale()
        setContentView(R.layout.activity_splash)
        //customEnableEdgeToEdge()

        customEnableEdgeToEdge2()


        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        if (isNetworkAvailable()){
            checkSubscription()
            //fetchRemoteConfigData()
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                moveNext()
            }
        }

    }

    fun fetchRemoteConfigData() {
        CoroutineScope(Dispatchers.IO).launch {
            RemoteConfigHelper(this@SplashActivity).firebaseRemoteFetch( object :
                RemoteConfigCallbackListiner {
                override fun onSuccess() {
                    remoteConfigResponse()
                }
                override fun onFailure() {
                    remoteConfigResponse()
                }
            })
        }
    }

    fun remoteConfigResponse(){
        initAdsConsent()
    }

    private fun initAdsConsent() {
        googleMobileAdsConsentManager = ConsentManager.getInstance(applicationContext)
        googleMobileAdsConsentManager.gatherConsent(this) { consentError ->
            if (consentError != null) {
                Log.d("consent", String.format("%s: %s", consentError.errorCode, consentError.message))
            }

            initializeMobileAdsSdk()

        }
    }

    fun initializeMobileAdsSdk(){
        MobileAds.initialize(this)
        showAdd()
        //moveNext()
    }

    fun checkSubscription() {
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .enablePrepaidPlans()
                    .build()
            )
            .setListener { billingResult, purchases ->
            }.build()

        establishConnection()
    }

    private fun establishConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    checkUserPremiumStatus()
                    checkInAppPurchases()
                } else {
                    fetchRemoteConfigData()
                }
            }

            override fun onBillingServiceDisconnected() {

            }
        })
    }

    private fun checkUserPremiumStatus1() {
        var isPremium = false

        fun finalizePremiumCheck() {
            PrefHelper.setIsPurchased(isPremium)
            fetchRemoteConfigData()
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient?.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    val hasValidProduct = purchase.products.contains(PREMIUM_WEEKLY_PACKAGE) || purchase.products.contains(PREMIUM_YEARLY_PACKAGE)

                    if (
                        hasValidProduct &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        purchase.isAcknowledged
                    ) {
                        isPremium = true
                        break
                    }
                }
            }
            finalizePremiumCheck()
        }
    }

    private fun checkUserPremiumStatus() {

        var isPremium = false
        var subsChecked = false
        var inAppChecked = false

        fun finalizeIfDone() {
            if (subsChecked && inAppChecked) {
                PrefHelper.setIsPurchased(isPremium)
                fetchRemoteConfigData()
            }
        }

        // 🔹 1️⃣ Check Subscriptions
        val subsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient?.queryPurchasesAsync(subsParams) { billingResult, purchases ->

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases?.forEach { purchase ->
                    val hasValidProduct =
                        purchase.products.contains(PREMIUM_WEEKLY_PACKAGE) ||
                                purchase.products.contains(PREMIUM_YEARLY_PACKAGE)

                    if (
                        hasValidProduct &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        purchase.isAcknowledged
                    ) {
                        isPremium = true
                    }
                }
            }

            subsChecked = true
            finalizeIfDone()
        }

        // 🔹 2️⃣ Check In-App (Lifetime)
        val inAppParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient?.queryPurchasesAsync(inAppParams) { billingResult, purchases ->

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases?.forEach { purchase ->

                    val hasValidProduct =
                        purchase.products.contains(PREMIUM_LIFETIME_PACKAGE_FOR_ADS)

                    if (
                        hasValidProduct &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        purchase.isAcknowledged
                    ) {
                        isPremium = true
                    }
                }
            }

            inAppChecked = true
            finalizeIfDone()
        }
    }


    private fun checkInAppPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient?.queryPurchasesAsync(params) { billingResult, purchases ->
            var isLifetimePurchased = false

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { purchase ->
                    val hasLifetime =
                        purchase.products.contains(PREMIUM_LIFETIME_PACKAGE)

                    if (
                        hasLifetime &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        purchase.isAcknowledged
                    ) {
                        isLifetimePurchased = true
                        return@forEach
                    }
                }
            }

            PrefHelper.setIsPurchasedLifeTime(isLifetimePurchased)
        }
    }

    fun showAdd(){
        if (PrefHelper.getIsPurchased() || !PrefHelper.getBooleanDefultTrue(splash_inter)){
            moveNext()
        } else {
            isIntertialAdshowing = true
            InterstitialAdManager(this).loadAndShowNewSplashAd(
                getString(R.string.splashInterstitialId),
                PrefHelper.getBooleanDefultTrue(splash_inter),
                object : InterstitialLoadCallback{
                    override fun onFailedToLoad() {
                        isIntertialAdshowing = false
                        Toast.makeText(this@SplashActivity, resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                        moveNext()
                    }
                    override fun onLoaded() {
                        isIntertialAdshowing = false
                        moveNext()
                    }
                }
            )
        }
    }


    fun moveNext(){
        /*val mIntent = if (user == null){
            if (prefHelper.getBooleanDefultTrue(isShowOnBoarding) && PrefHelper.getBooleanDefultTrue(is_show_onboarding_screen)){
                Intent(this, OnboardingActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
        } else {
            Intent(this, MainActivity::class.java)
        }*/

        val mIntent = if (PrefHelper.getIsPurchased() || !PrefHelper.getBooleanDefultTrue(is_show_iap_screen)){
            if (user?.isAnonymous == true) {
                Intent(this, MainActivity::class.java)
            } else if (user == null) {
                if (!PrefHelper.getBoolean(isLanguageSelected)){
                    Intent(this@SplashActivity, LanguageActivity::class.java)
                } else if (PrefHelper.getBooleanDefultTrue(isShowOnBoarding) && PrefHelper.getBooleanDefultTrue(is_show_onboarding_screen)){
                    Intent(this, OnboardingActivity::class.java)
                } else {
                    Intent(this, LoginActivity::class.java)
                }
            } else {
                Intent(this, MainActivity::class.java)
            }
        } else {
            //proIntent()

            if (user?.isAnonymous == true) {
                proIntent()
            } else if (user == null) {
                if (!PrefHelper.getBoolean(isLanguageSelected)){
                    Intent(this@SplashActivity, LanguageActivity::class.java)
                } else if (PrefHelper.getBooleanDefultTrue(isShowOnBoarding) && PrefHelper.getBooleanDefultTrue(is_show_onboarding_screen)){
                    Intent(this, OnboardingActivity::class.java)
                } else {
                    //Intent(this, LoginActivity::class.java)
                    proIntent()
                }
            } else {
                proIntent()
            }
        }



        mIntent.apply {
            putExtra("from", "splash")
        }

        startActivity(mIntent)
        finish()
    }
}