package com.app.styletap.webtoappconverter.presentations.ui.activities.createApp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.app.styletap.webtoappconverter.MyApplication
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityCreateAppBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.isNetworkAvailable
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ACTION_FINISH_ACTIVITY
import com.app.styletap.webtoappconverter.presentations.utils.Contants.createapp_native
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper

class CreateAppActivity : AppCompatActivity() {
    lateinit var binding: ActivityCreateAppBinding
    lateinit var prefHelper: PrefHelper


    private val finishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_FINISH_ACTIVITY) {
                finish()
            }
        }
    }

    private val adObserver = {
        runOnUiThread {
            showNativeAd()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeLocale()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityCreateAppBinding.inflate(layoutInflater)
        setContentView(binding.root)
        customEnableEdgeToEdge()

        adjustTopHeight(binding.toolbarLL)
        adjustBottomHeight(binding.container)

        prefHelper = PrefHelper(this)
        val app = application as MyApplication

        if (isNetworkAvailable() && prefHelper.getBooleanDefultTrue(createapp_native) && !prefHelper.getIsPurchased()) {

            // Start shimmer
            binding.shimmerContainer.nativeShimmerView.startShimmer()
            binding.shimmerContainer.nativeShimmerView.visibility = View.VISIBLE
            binding.adParentLayout.visibility = View.VISIBLE
            binding.nativeLayout.visibility = View.VISIBLE

            // Add observer for ad loaded
            app.nativeAdManager.addAdLoadedListener(adObserver)

            // Load ad if not already loaded
            app.nativeAdManager.loadNativeAdIfNeeded(this,getString(R.string.createAppScreenNativeId))

            // Show immediately if already loaded
            showNativeAd()

        } else {
            // Hide ad layout if conditions not met
            binding.adParentLayout.visibility = View.GONE
            binding.shimmerContainer.nativeShimmerView.stopShimmer()
        }


        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBack()
                }
            })

        initView()

        registerReceiver(
            finishReceiver,
            IntentFilter(ACTION_FINISH_ACTIVITY),
            Context.RECEIVER_NOT_EXPORTED // required for Android 13+
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(finishReceiver)
        }catch (_: Exception){}

        try {
            (application as MyApplication).nativeAdManager.removeAdLoadedListener(adObserver)
        }catch (_: Exception){}

        try {
            (application as MyApplication).nativeAdManager.clearAd()
        }catch (_: Exception){}

    }

    fun onBack() {
        finish()
    }

    fun initView() {
        binding.apply {
            toolbar.titleTv.text = resources.getString(R.string.create_app)
            toolbar.backBtn.isVisible = true
            toolbar.backBtn.setOnClickListener {
                onBack()
            }

            nextBtn.setOnClickListener {
                val url = etUrl.text.toString().trim()
                if (!invalidateUrl(url)) return@setOnClickListener

                val mIntent = Intent(this@CreateAppActivity, AppBasicInfoActivity::class.java).apply {
                    putExtra("webUrl", url)
                }
                moveNext(mIntent)
            }

        }

    }

    fun invalidateUrl(url: String): Boolean {
        binding.apply {

            if (url.isEmpty()) {
                etUrl.apply {
                    requestFocus()
                    error = context.getString(R.string.please_enter_url)
                }
                return false
            }

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                etUrl.apply {
                    requestFocus()
                    error = context.getString(R.string.url_must_start_with_http_or_https)
                }
                return false
            }

            if (!Patterns.WEB_URL.matcher(url).matches()) {
                etUrl.apply {
                    requestFocus()
                    error = context.getString(R.string.invalid_web_url)
                }
                return false
            }

            return true
        }

    }

    fun moveNext(intent: Intent){
        startActivity(intent)
    }


    private fun showNativeAd() {
        val app = application as MyApplication

        if (app.nativeAdManager.nativeAd == null) return

        // Stop shimmer
        binding.shimmerContainer.nativeShimmerView.stopShimmer()
        binding.shimmerContainer.nativeShimmerView.visibility = View.GONE

        binding.adParentLayout.visibility = View.VISIBLE
        binding.nativeLayout.visibility = View.VISIBLE

        // Populate ad
        app.nativeAdManager.showNativeAd(
            this,
            binding.adFrame,
            binding.shimmerContainer.nativeShimmerView
        )
    }

}