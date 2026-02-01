package com.app.styletap.webtoappconverter.presentations.ui.activities.createApp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.app.styletap.webtoappconverter.MyApplication
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityAppBasicInfoBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.isNetworkAvailable
import com.app.styletap.webtoappconverter.extentions.isValidPackageName
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ACTION_FINISH_ACTIVITY
import com.app.styletap.webtoappconverter.presentations.utils.Contants.createapp_native
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper

class AppBasicInfoActivity : AppCompatActivity() {
    lateinit var binding: ActivityAppBasicInfoBinding

    var webUrl = ""

    var appOrientation1: String = "Portrait"

    private val finishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_FINISH_ACTIVITY) {
                finish()
            }
        }
    }

    lateinit var prefHelper: PrefHelper

    private val adObserver = {
        runOnUiThread {
            showNativeAd()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeLocale()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityAppBasicInfoBinding.inflate(layoutInflater)
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
            //app.nativeAdManager.loadNativeAdIfNeeded(this,getString(R.string.createAppScreenNativeId))

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

        intent.extras?.let {
            webUrl = it.getString("webUrl", "")
        }


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


    }

    fun onBack() {
        finish()
    }

    fun initView() {
        binding.apply {
            toolbar.titleTv.text = resources.getString(R.string.app_basic_info)
            toolbar.backBtn.isVisible = true
            toolbar.backBtn.setOnClickListener {
                onBack()
            }

            val color = ContextCompat.getColor(this@AppBasicInfoActivity, R.color.grey_2)

            portraitBtn.setOnClickListener {
                ViewCompat.setBackgroundTintList(portraitCL, null)
                ViewCompat.setBackgroundTintList(landscapeCL, ColorStateList.valueOf(color))

                portraitTv.setTextColor(ContextCompat.getColor(this@AppBasicInfoActivity, R.color.white))
                landscapeTv.setTextColor(ContextCompat.getColor(this@AppBasicInfoActivity, R.color.black))

                appOrientation1 = "Portrait"
            }

            landscapeBtn.setOnClickListener {
                ViewCompat.setBackgroundTintList(portraitCL, ColorStateList.valueOf(color))
                ViewCompat.setBackgroundTintList(landscapeCL, null)

                portraitTv.setTextColor(ContextCompat.getColor(this@AppBasicInfoActivity, R.color.black))
                landscapeTv.setTextColor(ContextCompat.getColor(this@AppBasicInfoActivity, R.color.white))

                appOrientation1 = "Landscape"

            }

            nextBtn.setOnClickListener {
                val appName = etAppName.text.toString().trim()
                val packageName1 = etPackageName.text.toString().trim()

                if (!invalidateInputs(appName, packageName1)) return@setOnClickListener

                val mIntent = Intent(this@AppBasicInfoActivity, AppBrandingActivity::class.java).apply {
                    putExtra("webUrl", webUrl)
                    putExtra("appName", appName)
                    putExtra("packageName1", packageName1)
                    putExtra("appOrientation1", appOrientation1)

                }
                moveNext(mIntent)

            }

        }
    }

    fun invalidateInputs(appName: String, packageName1: String): Boolean {
        binding.apply {

            if (appName.isEmpty() || appName.length < 3) {
                etAppName.apply {
                    requestFocus()
                    error = context.getString(R.string.app_name_must_be_at_least_3_letters_and_contain_only_alphabets)
                }
                return false
            }

            if (!isValidPackageName(packageName1)) {
                etPackageName.apply {
                    requestFocus()
                    error = context.getString(R.string.the_package_name_must_follow_the_format_com_example_app)
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