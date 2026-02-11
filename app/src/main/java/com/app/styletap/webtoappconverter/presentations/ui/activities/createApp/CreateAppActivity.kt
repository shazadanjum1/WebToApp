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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.app.styletap.ads.BannerAdManager
import com.app.styletap.interfaces.FirebaseAnalyticsUtils
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityCreateAppBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.isNetworkAvailable
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ACTION_FINISH_ACTIVITY
import com.app.styletap.webtoappconverter.presentations.utils.Contants.createapp_banner
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper

class CreateAppActivity : AppCompatActivity() {
    lateinit var binding: ActivityCreateAppBinding

    private val finishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_FINISH_ACTIVITY) {
                finish()
            }
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
        FirebaseAnalyticsUtils.logEventMessage("url_screen_view")

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.scrollView.setPadding(
                0,
                0,
                0,
                imeInsets.bottom
            )
            insets
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

        showBannerAd()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(finishReceiver)
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
                FirebaseAnalyticsUtils.logEventMessage("url_validation_success")

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
            } else {
                FirebaseAnalyticsUtils.logEventMessage("url_entered")
            }

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                FirebaseAnalyticsUtils.logEventMessage("url_validation_failed")
                etUrl.apply {
                    requestFocus()
                    error = context.getString(R.string.url_must_start_with_http_or_https)
                }
                return false
            }

            if (!Patterns.WEB_URL.matcher(url).matches()) {
                FirebaseAnalyticsUtils.logEventMessage("url_validation_failed")
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


    fun showBannerAd(){
        if (isNetworkAvailable() && PrefHelper.getBooleanDefultTrue(createapp_banner) && !PrefHelper.getIsPurchased()){
            binding.adLayout.visibility = View.VISIBLE
            binding.bannerShimmerView.root.visibility = View.VISIBLE
            binding.bannerShimmerView.bannerShimmerView.startShimmer()
            BannerAdManager(this).loadAndShowBannerAd(resources.getString(R.string.createappBannerId) , binding.adFrame, binding.adLayout, binding.bannerShimmerView.bannerShimmerView)
        } else {
            binding.adLayout.visibility = View.GONE
            binding.bannerShimmerView.bannerShimmerView.stopShimmer()
        }
    }
}