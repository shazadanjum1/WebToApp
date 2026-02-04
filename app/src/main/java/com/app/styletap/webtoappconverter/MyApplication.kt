package com.app.styletap.webtoappconverter

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.app.styletap.ads.NativeAdManager
import com.app.styletap.interfaces.AppOpenAdCallBack
import com.app.styletap.interfaces.FirebaseAnalyticsUtils
import com.app.styletap.webtoappconverter.presentations.ui.activities.splash.SplashActivity
import com.app.styletap.webtoappconverter.presentations.utils.Contants.app_open
import com.app.styletap.webtoappconverter.presentations.utils.Contants.isIntertialAdshowing
import com.app.styletap.webtoappconverter.presentations.utils.Contants.isShowApOpenAd
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

import java.util.Date

private const val LOG_TAG = "AppOpenAd"

class MyApplication : Application(), Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver { //LifecycleObserver

    private lateinit var appOpenAdManager: AppOpenAdManager
    private var currentActivity: Activity? = null

    lateinit var nativeAdManager: NativeAdManager

    override fun onCreate() {
        //super.onCreate()
        super<Application>.onCreate()

        nativeAdManager = NativeAdManager(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        FirebaseApp.initializeApp(this)
        FirebaseAnalytics.getInstance(this)
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false

        try{
            MobileAds.initialize(this) { }
        }catch (_: Exception){}

        registerActivityLifecycleCallbacks(this)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        appOpenAdManager = AppOpenAdManager()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d(LOG_TAG, "App moved to foreground")
        onMoveToForeground()
    }

    private fun onMoveToForeground() {
        Log.d(LOG_TAG, "onMoveToForeground")
        if (
            !PrefHelper(applicationContext).getIsPurchased() &&
            currentActivity !is SplashActivity &&
            currentActivity !is AdActivity &&
            !isIntertialAdshowing &&
            isShowApOpenAd &&
            PrefHelper(applicationContext).getBooleanDefultTrue(app_open)
        ) {
            currentActivity?.let {
                Handler(Looper.myLooper()!!).postDelayed({
                    appOpenAdManager.showAdIfAvailable(it, getString(R.string.appOpenId))
                }, 100)
            }
        }
    }

    /*@OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        Log.d(LOG_TAG, "onMoveToForeground")
        //!PreferencesHelper(this).getProUser() &&
        if (currentActivity !is SplashActivity && currentActivity !is AdActivity && !isIntertialAdshowing && isShowApOpenAd &&  PrefHelper(applicationContext).getBooleanDefultTrue(app_open)) {
            currentActivity?.let {
                Handler(Looper.myLooper()!!).postDelayed({
                    appOpenAdManager.showAdIfAvailable(it, getString(R.string.appOpenId))
                }, 100)
            }
        }
    }*/


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {
        Log.d(LOG_TAG, "onActivityResumed")
        // && !PreferencesHelper(applicationContext).getProUser()
        if (!PrefHelper(applicationContext).getIsPurchased() && appOpenAdManager.appOpenAd == null && currentActivity !is SplashActivity && currentActivity !is AdActivity && PrefHelper(applicationContext).getBooleanDefultTrue(app_open)) {
            loadAd(currentActivity!!, getString(R.string.appOpenId))
        }
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}


    fun loadAd(activity: Activity, AD_UNIT_ID: String) {
        appOpenAdManager.loadAd(activity, AD_UNIT_ID, object : AppOpenAdCallBack {
            override fun onLoaded() {}
            override fun onFailedToLoaded() {}
        })
    }

    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }

    class AppOpenAdManager {
        var loadingDialog: Dialog? = null
        var animationIcon: ImageView? = null

        var appOpenAd: AppOpenAd? = null
        private var isLoadingAd = false
        var isShowingAd = false

        private var loadTime: Long = 0

        fun loadAd(context: Context, AD_UNIT_ID: String, appOpenAdsCallBack: AppOpenAdCallBack) {
            if (isLoadingAd || isAdAvailable()) {
                return
            }

            isLoadingAd = true
            val request = AdRequest.Builder().build()
            AppOpenAd.load(
                context,
                AD_UNIT_ID,
                request,
                object : AppOpenAd.AppOpenAdLoadCallback() {

                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        isLoadingAd = false
                        loadTime = Date().time
                        Log.d(LOG_TAG, "onAdLoaded.")
                        FirebaseAnalyticsUtils.logEventMessage("AppOpenAd_Loaded")

                        appOpenAdsCallBack.onLoaded()
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isLoadingAd = false
                        Log.d(LOG_TAG, "onAdFailedToLoad: " + loadAdError.message)
                        appOpenAdsCallBack.onFailedToLoaded()
                        FirebaseAnalyticsUtils.logEventMessage("AppOpenAd_FailedToLoad")

                    }
                }
            )
        }

        private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
            val dateDifference: Long = Date().time - loadTime
            val numMilliSecondsPerHour: Long = 3600000
            return dateDifference < numMilliSecondsPerHour * numHours
        }

        private fun isAdAvailable(): Boolean {
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
        }

        fun showAdIfAvailable(activity: Activity, AD_UNIT_ID: String) {
            showAdIfAvailable(
                activity,
                AD_UNIT_ID,
                object : OnShowAdCompleteListener {
                    override fun onShowAdComplete() {
                    }
                }
            )
        }

        fun showAdIfAvailable(
            activity: Activity,
            AD_UNIT_ID: String,
            onShowAdCompleteListener: OnShowAdCompleteListener
        ) {
            if (isShowingAd) {
                Log.d(LOG_TAG, "The app open ad is already showing.")
                return
            }

            if (!isAdAvailable()) {
                Log.d(LOG_TAG, "The app open ad is not ready yet.")
                onShowAdCompleteListener.onShowAdComplete()
                loadAd(activity, AD_UNIT_ID, object : AppOpenAdCallBack {
                    override fun onLoaded() {}

                    override fun onFailedToLoaded() {}

                })

                return
            }

            Log.d(LOG_TAG, "Will show ad.")

            appOpenAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        appOpenAd = null
                        isShowingAd = false

                        isShowApOpenAd = true
                        isIntertialAdshowing = false

                        FirebaseAnalyticsUtils.logEventMessage("AppOpenAd_Dismissed")

                        if (loadingDialog != null && loadingDialog!!.isShowing) try {
                            loadingDialog!!.dismiss()
                        } catch (_: Exception) {}



                        onShowAdCompleteListener.onShowAdComplete()
                        loadAd(activity, AD_UNIT_ID, object : AppOpenAdCallBack {
                            override fun onLoaded() {}

                            override fun onFailedToLoaded() {}

                        })

                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        isShowingAd = false

                        isShowApOpenAd = true
                        isIntertialAdshowing = false

                        if (loadingDialog != null && loadingDialog!!.isShowing) try {
                            loadingDialog!!.dismiss()
                        } catch (_: Exception) {}

                        if (adError.message != "The ad can not be shown when app is not in foreground.") {
                            appOpenAd = null
                        }

                        Log.d(LOG_TAG, "onAdFailedToShowFullScreenContent: " + adError.message)
                        FirebaseAnalyticsUtils.logEventMessage("AppOpenAd_FailedToShow")

                        onShowAdCompleteListener.onShowAdComplete()
                        loadAd(activity, AD_UNIT_ID, object : AppOpenAdCallBack {
                            override fun onLoaded() {}
                            override fun onFailedToLoaded() {}
                        })

                    }

                    override fun onAdShowedFullScreenContent() {
                        isShowApOpenAd = true
                        isIntertialAdshowing = false
                        FirebaseAnalyticsUtils.logEventMessage("AppOpenAd_Showing")

                    }
                }

            show_animation_dialoug(activity)

            Handler(Looper.myLooper()!!).postDelayed({
                appOpenAd?.show(activity)
                isShowingAd = true
            }, 100)

            /*isShowingAd = true
            appOpenAd?.show(activity)*/
        }

        private fun show_animation_dialoug(activity: Activity?) {
            loadingDialog = Dialog(activity!!)
            loadingDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            loadingDialog!!.setContentView(R.layout.loading_app_open_ad_layout)
            loadingDialog!!.setCancelable(false)
            loadingDialog!!.setCanceledOnTouchOutside(false)
            loadingDialog!!.show()
            animationIcon = loadingDialog!!.findViewById(R.id.icon_animation)
            loadingDialog!!.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            animationIcon!!.startAnimation(
                AnimationUtils.loadAnimation(
                    activity,
                    R.anim.app_open_ad_ani
                )
            )
        }

    }
}