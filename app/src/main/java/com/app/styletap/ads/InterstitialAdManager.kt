package com.app.styletap.ads

import android.app.Activity
import android.app.Dialog
import com.app.styletap.interfaces.InterstitialCallback
import com.app.styletap.interfaces.InterstitialLoadCallback
import com.app.styletap.webtoappconverter.extentions.adLoadingDialog
import com.app.styletap.webtoappconverter.extentions.isNetworkAvailable
import com.app.styletap.webtoappconverter.presentations.utils.Contants.isIntertialAdshowing
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class InterstitialAdManager (val mActivity: Activity) {

    companion object{
        var mInterstitialAd: InterstitialAd? = null
        var adLoading: Boolean = false
        var counter = 0
        //var adCounter = 2
    }
    /*var prefHelper: PrefHelper

    init {
        prefHelper = PrefHelper(mActivity)
    }*/

    fun loadInterstitialAds(ID: String){

        if ( adLoading || mInterstitialAd != null || !mActivity.isNetworkAvailable() || ID == "") {
            return
        }
        adLoading = true

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(mActivity, ID, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                    adLoading = false
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    mInterstitialAd = ad
                    adLoading = false
                }
            }
        )
    }

    fun isAdsAvailable(): Boolean {
        if (mInterstitialAd != null){
            return true
        }
        return false
    }

    fun updateCounterAndLoadAd(){
        counter++
        //loadInterstitialAds(prefHelper.getString(Constants.normal_interstitial_id, ""))
    }


    fun showAndLoadInterstitialAd(ID: String, isShow: Boolean, interstitialCallback: InterstitialCallback){

        counter++
        if (mInterstitialAd == null && mActivity.isNetworkAvailable() && !adLoading){
            loadInterstitialAds(ID)
            interstitialCallback.failedToShow()
            return
        }


        if ( adLoading || !mActivity.isNetworkAvailable() ||  !isShow ) { //counter < prefHelper.getAdCounter() ||
            interstitialCallback.failedToShow()
            return
        }

        val adsDialog = mActivity.adLoadingDialog()

        mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdClicked() {}

            override fun onAdDismissedFullScreenContent() {
                isIntertialAdshowing = false
                counter = 0
                mInterstitialAd = null
                //loadInterstitialAds(ID)
                interstitialCallback.onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                isIntertialAdshowing = false
                mInterstitialAd = null
                //loadInterstitialAds(ID)
                interstitialCallback.failedToShow()
            }

            override fun onAdImpression() {}

            override fun onAdShowedFullScreenContent() {}

        }

        if (mInterstitialAd != null) {
            CoroutineScope(Dispatchers.Main).launch {
                isIntertialAdshowing = true
                delay(600)
                mInterstitialAd?.show(mActivity)
                delay(500)
                adsDialog.safeDismiss(mActivity)
            }

        } else {
            adsDialog.safeDismiss(mActivity)
            loadInterstitialAds(ID)
            interstitialCallback.failedToShow()
        }
    }

    fun loadWithCallback(ID: String, interstitialLoadCallback: InterstitialLoadCallback){

        if (!mActivity.isNetworkAvailable()) {
            interstitialLoadCallback.onFailedToLoad()
            return
        }

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(mActivity, ID, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                    adLoading = false
                    interstitialLoadCallback.onFailedToLoad()
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    //counter = 0
                    mInterstitialAd = ad
                    adLoading = false
                    interstitialLoadCallback.onLoaded()
                }
            }
        )
    }


    fun loadAndShowSplashAd(ID: String, isShow: Boolean, interstitialLoadCallback: InterstitialLoadCallback){
        //counter = prefHelper.getAdCounter()+2

        if (!mActivity.isNetworkAvailable() || !isShow) {
            interstitialLoadCallback.onFailedToLoad()
            return
        }

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(mActivity, ID, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                    adLoading = false
                    interstitialLoadCallback.onFailedToLoad()
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    mInterstitialAd = ad
                    adLoading = false


                    mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                        override fun onAdClicked() {}

                        override fun onAdDismissedFullScreenContent() {
                            mInterstitialAd = null
                            //loadInterstitialAds(prefHelper.getString(Constants.normal_interstitial_id, ""))
                            interstitialLoadCallback.onLoaded()
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            mInterstitialAd = null
                            //loadInterstitialAds(prefHelper.getString(Constants.normal_interstitial_id, ""))
                            interstitialLoadCallback.onFailedToLoad()
                        }

                        override fun onAdImpression() {}

                        override fun onAdShowedFullScreenContent() {}

                    }

                    if (mInterstitialAd != null) {
                        mInterstitialAd?.show(mActivity)
                    } else {
                        loadInterstitialAds(ID)
                        interstitialLoadCallback.onFailedToLoad()
                    }
                }
            }
        )
    }


    fun loadAndShowAd(ID: String, isShow: Boolean, interstitialLoadCallback: InterstitialLoadCallback){

        if (!mActivity.isNetworkAvailable()) { //  || !isShow || prefHelper.getIsPurchased()
            interstitialLoadCallback.onFailedToLoad()
            return
        }

        val adsDialog = mActivity.adLoadingDialog()

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(mActivity, ID, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adsDialog.safeDismiss(mActivity)
                    mInterstitialAd = null
                    adLoading = false
                    interstitialLoadCallback.onFailedToLoad()
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    mInterstitialAd = ad
                    adLoading = false


                    mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                        override fun onAdClicked() {}

                        override fun onAdDismissedFullScreenContent() {
                            mInterstitialAd = null
                            interstitialLoadCallback.onLoaded()
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            mInterstitialAd = null
                            interstitialLoadCallback.onFailedToLoad()
                        }

                        override fun onAdImpression() {}

                        override fun onAdShowedFullScreenContent() {}

                    }

                    adsDialog.safeDismiss(mActivity)
                    if (mInterstitialAd != null) {
                        mInterstitialAd?.show(mActivity)
                    } else {
                        interstitialLoadCallback.onFailedToLoad()
                    }
                }
            }
        )
    }


    fun Dialog?.safeDismiss(activity: Activity?) {
        if (this != null && this.isShowing && activity != null && !activity.isFinishing && !activity.isDestroyed) {
            try {
                dismiss()
            } catch (e: IllegalStateException) { }
            catch (e: IllegalArgumentException) { }
            catch (e: Exception) { }
        }
    }
}