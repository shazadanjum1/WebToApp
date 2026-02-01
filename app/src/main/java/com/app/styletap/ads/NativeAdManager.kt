package com.app.styletap.ads

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.app.styletap.interfaces.NativeAdLoadCallback
import com.app.styletap.webtoappconverter.databinding.NativeAdMediumBinding
import com.app.styletap.webtoappconverter.extentions.isNetworkAvailable
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NativeAdManager(val activity: Context) {

    var nativeAd: NativeAd? = null
        private set

    var adIsLoading: Boolean = false

    fun isNativeAdAvailable(): Boolean {
        if (nativeAd != null){
            return true
        }
        return false
    }

    fun clearAd() {
        nativeAd?.destroy()
        nativeAd = null
    }


    private val listeners = mutableListOf<() -> Unit>()

    // Add a listener (Activity calls this to get notified)
    fun addAdLoadedListener(listener: () -> Unit) {
        listeners.add(listener)
        // If ad is already loaded, notify immediately
        if (nativeAd != null) listener()
    }

    fun removeAdLoadedListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyAdLoaded() {
        listeners.forEach { it() }
    }

    fun loadNativeAdIfNeeded(activity: Activity, adUnitId: String) {
        if (adIsLoading || nativeAd != null || !activity.isNetworkAvailable()) return

        adIsLoading = true

        val adLoader = AdLoader.Builder(activity, adUnitId)
            .forNativeAd { ad ->
                nativeAd = ad
                adIsLoading = false
                notifyAdLoaded() // <-- observer notified here
            }
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setVideoOptions(VideoOptions.Builder().setStartMuted(false).build())
                    .build()
            )
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    nativeAd = null
                    adIsLoading = false
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }
    fun showNativeAd(activity: Activity, adFrame: FrameLayout, shimmer: ShimmerFrameLayout) {
        nativeAd?.let { ad ->
            shimmer.stopShimmer()
            shimmer.visibility = View.GONE

            val binding = NativeAdMediumBinding.inflate(activity.layoutInflater)

            val adView = binding.root
            adView.headlineView = binding.adHeadline
            adView.bodyView = binding.adBody
            adView.callToActionView = binding.adCallToAction
            adView.mediaView = binding.adMedia

            binding.adHeadline.text = ad.headline
            binding.adBody.text = ad.body
            binding.adCallToAction.text = ad.callToAction

            ad.mediaContent?.let { binding.adMedia.setMediaContent(it) }

            adView.setNativeAd(ad)

            adFrame.removeAllViews()
            adFrame.addView(adView)
        }
    }


    fun loadNativeAd(activity: Activity, nativeId: String, nativeAdLoadCallback: NativeAdLoadCallback) {

        if ( adIsLoading || !activity.isNetworkAvailable() || nativeAd != null || PrefHelper(activity.applicationContext).getIsPurchased()){
            return
        }

        val builder = AdLoader.Builder(activity, nativeId)

        builder.forNativeAd { nativeAdd ->
            nativeAd = nativeAdd
        }

        val videoOptions =
            VideoOptions.Builder().setStartMuted(false).build()

        val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()

        builder.withNativeAdOptions(adOptions)

        val adLoader =
            builder
                .withAdListener(
                    object : AdListener() {
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            adIsLoading = false
                            nativeAd = null
                            nativeAdLoadCallback.onNativeAdFailedLoad()
                        }

                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            adIsLoading = false
                            nativeAdLoadCallback.onNativeAdLoaded()
                        }
                    }
                )
                .build()

        adLoader.loadAd(AdRequest.Builder().build())

    }


    fun loadAndPopulateNativeAdView(
        activity: Activity,
        nativeId: String,
        adFrame: FrameLayout,
        layout: Int,
        shimmerViewContainer: ShimmerFrameLayout
    ) {

        if (!activity.isNetworkAvailable() || PrefHelper(activity.applicationContext).getIsPurchased()){
            return
        }

        val builder = AdLoader.Builder(activity, nativeId)

        builder.forNativeAd { nativeAdd ->
            nativeAd = nativeAdd
        }

        val videoOptions =
            VideoOptions.Builder().setStartMuted(false).build()

        val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()

        builder.withNativeAdOptions(adOptions)

        val adLoader =
            builder
                .withAdListener(
                    object : AdListener() {
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            adIsLoading = false
                            nativeAd = null
                            CoroutineScope(Dispatchers.Main).launch {
//                                shimmerViewContainer.stopShimmer();
//                                shimmerViewContainer.visibility = View.GONE
                            }
                        }

                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            adIsLoading = false
                            CoroutineScope(Dispatchers.Main).launch {
                                shimmerViewContainer.stopShimmer();
                                shimmerViewContainer.visibility = View.GONE
                                populateNativeAdView(activity, adFrame, layout)
                            }

                        }
                    }
                )
                .build()

        adLoader.loadAd(AdRequest.Builder().build())

    }


    fun populateNativeAdView( activity: Activity, adFrame: FrameLayout, layout: Int) {
        if ( adIsLoading || !activity.isNetworkAvailable() || nativeAd == null || PrefHelper(activity.applicationContext).getIsPurchased()){
            return
        }

        populateNativeMediumAd(adFrame, NativeAdMediumBinding.inflate(activity.layoutInflater))

    }


    fun populateNativeMediumAd(adFrame: FrameLayout, unifiedAdBinding: NativeAdMediumBinding) {

        val nativeAdView = unifiedAdBinding.root

        nativeAdView.headlineView = unifiedAdBinding.adHeadline
        nativeAdView.bodyView = unifiedAdBinding.adBody
        nativeAdView.callToActionView = unifiedAdBinding.adCallToAction
        unifiedAdBinding.adHeadline.text = nativeAd?.headline
        nativeAdView.mediaView = unifiedAdBinding.adMedia

        nativeAd?.mediaContent?.let { unifiedAdBinding.adMedia.setMediaContent(it) }


        if (nativeAd?.body == null) {
            unifiedAdBinding.adBody.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adBody.visibility = View.VISIBLE
            unifiedAdBinding.adBody.text = nativeAd?.body
        }

        if (nativeAd?.callToAction == null) {
            unifiedAdBinding.adCallToAction.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adCallToAction.visibility = View.VISIBLE
            unifiedAdBinding.adCallToAction.text = nativeAd?.callToAction
        }


        nativeAd?.let { nativeAdView.setNativeAd(it) }

        val mediaContent = nativeAd?.mediaContent
        val vc = mediaContent?.videoController
        if (vc != null && mediaContent.hasVideoContent()) {
            vc.videoLifecycleCallbacks =
                object : VideoController.VideoLifecycleCallbacks() {
                    override fun onVideoEnd() {
                        super.onVideoEnd()
                    }
                }
        }

        adFrame.removeAllViews()
        adFrame.addView(unifiedAdBinding.root)
    }


}