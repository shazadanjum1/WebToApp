package com.app.styletap.webtoappconverter.presentations.ui.activities.splash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.app.styletap.ads.ConsentManager
import com.app.styletap.interfaces.RemoteConfigCallbackListiner
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.isNetworkAvailable
import com.app.styletap.webtoappconverter.firebase.RemoteConfigHelper
import com.app.styletap.webtoappconverter.presentations.ui.activities.authorization.LoginActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.home.MainActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.language.LanguageActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.onboarding.OnboardingActivity
import com.app.styletap.webtoappconverter.presentations.utils.Contants.isLanguageSelected
import com.app.styletap.webtoappconverter.presentations.utils.Contants.isShowOnBoarding
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*
pro screen (subscription)
splash intertials ads
pro screen locale with device locale
pro screen (IAP)
 */

class SplashActivity : AppCompatActivity() {
    private lateinit var prefHelper: PrefHelper

    private lateinit var auth: FirebaseAuth
    var user: FirebaseUser? = null


    private lateinit var googleMobileAdsConsentManager: ConsentManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeLocale()
        setContentView(R.layout.activity_splash)
        customEnableEdgeToEdge()

        prefHelper = PrefHelper(this.applicationContext)
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser


        if (isNetworkAvailable()){
            fetchRemoteConfigData()
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
        moveNext()
    }



    fun moveNext(){
        /*val mIntent = if (user == null){
            if (prefHelper.getBooleanDefultTrue(isShowOnBoarding)){
                Intent(this, OnboardingActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
        } else {
            Intent(this, MainActivity::class.java)
        }*/

        val mIntent =
            if (user?.isAnonymous == true) {
                Intent(this, MainActivity::class.java)
            } else if (user == null) {
                if (!prefHelper.getBoolean(isLanguageSelected)){
                    Intent(this@SplashActivity, LanguageActivity::class.java)
                } else if (prefHelper.getBooleanDefultTrue(isShowOnBoarding)){
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
}