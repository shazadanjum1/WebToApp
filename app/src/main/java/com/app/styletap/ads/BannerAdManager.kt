package com.app.styletap.ads

import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.app.styletap.webtoappconverter.extentions.isNetworkAvailable
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

class BannerAdManager(val mActivity: Activity) {
    lateinit var mAdFrameLayout: FrameLayout

    fun loadAndShowBannerAd(
        id: String,
        frameLayout: FrameLayout,
        relativeLayout: RelativeLayout,
        shimmerBannerContainer: ShimmerFrameLayout,
    ){
        mAdFrameLayout = frameLayout

        if (mActivity.isNetworkAvailable()) {
            relativeLayout.visibility = View.VISIBLE

            val adView = AdView(mActivity)
            adView.setAdSize(adSize)
            adView.adUnitId = id

            frameLayout.removeAllViews()
            frameLayout.addView(adView)

            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)

            adView.adListener = object: AdListener() {
                override fun onAdClicked() {}

                override fun onAdClosed() {}

                override fun onAdFailedToLoad(adError : LoadAdError) {
                    shimmerBannerContainer.stopShimmer()
                    shimmerBannerContainer.visibility = View.GONE
                }

                override fun onAdImpression() {}

                override fun onAdLoaded() {
                    shimmerBannerContainer.stopShimmer()
                    shimmerBannerContainer.visibility = View.GONE
                }

                override fun onAdOpened() {}
            }
        } else {
            relativeLayout.visibility = View.GONE
        }
    }

    fun loadAndShowCollapsibleBanner(
        id: String,
        collapsible: String,
        frameLayout: FrameLayout,
        relativeLayout: RelativeLayout,
        shimmerBannerContainer: ShimmerFrameLayout
    ){
        mAdFrameLayout = frameLayout
        if (mActivity.isNetworkAvailable()) {
            relativeLayout.visibility = View.VISIBLE

            val adView = AdView(mActivity)
            adView.setAdSize(adSize)
            adView.adUnitId = id

            frameLayout.removeAllViews()
            frameLayout.addView(adView)

            val extras = Bundle()
            extras.putString("collapsible", collapsible)

            val adRequest = AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                .build()

            adView.loadAd(adRequest)

            adView.adListener = object: AdListener() {
                override fun onAdClicked() {}

                override fun onAdClosed() {}

                override fun onAdFailedToLoad(adError : LoadAdError) {
                    shimmerBannerContainer.stopShimmer()
                    shimmerBannerContainer.visibility = View.GONE
                }

                override fun onAdImpression() {}

                override fun onAdLoaded() {
                    shimmerBannerContainer.stopShimmer()
                    shimmerBannerContainer.visibility = View.GONE
                }

                override fun onAdOpened() {}
            }
        } else {
            relativeLayout.visibility = View.GONE
        }
    }

    private val adSize: AdSize
        get(){
            val display = mActivity.windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = mAdFrameLayout.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mActivity, adWidth)
        }

}