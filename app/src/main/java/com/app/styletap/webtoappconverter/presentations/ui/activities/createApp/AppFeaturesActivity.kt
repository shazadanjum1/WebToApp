package com.app.styletap.webtoappconverter.presentations.ui.activities.createApp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityAppFeaturesBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.changeIcon
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ACTION_FINISH_ACTIVITY

class AppFeaturesActivity : AppCompatActivity() {
    lateinit var binding: ActivityAppFeaturesBinding

    var webUrl = ""
    var appName = ""
    var packageName1 = ""
    var appOrientation1 = ""

    var primaryColor = ""
    var secondaryColor = ""
    var imageUri: Uri? = null


    var isPullToRefresh = true
    var isLoaderScreen = true
    var isFullScreenMode = false
    var isNavigationButtons = true
    var isExternalLinksInBrowser = true
    var isNotificationSupport = false

    private val finishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_FINISH_ACTIVITY) {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityAppFeaturesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        customEnableEdgeToEdge()

        adjustTopHeight(binding.toolbarLL)
        adjustBottomHeight(binding.container)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBack()
                }
            })

        intent.extras?.let {
            webUrl = it.getString("webUrl", "")
            appName = it.getString("appName", "")
            packageName1 = it.getString("packageName1", "")
            appOrientation1 = it.getString("appOrientation1", "")

            primaryColor = it.getString("primaryColor", "")
            secondaryColor = it.getString("secondaryColor", "")
            appOrientation1 = it.getString("appOrientation1", "")

            imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("imageUri", Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<Uri>("imageUri")
            }


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
    }

    fun onBack() {
        finish()
    }

    fun initView() {
        binding.apply {
            toolbar.titleTv.text = resources.getString(R.string.app_features)
            toolbar.backBtn.isVisible = true
            toolbar.backBtn.setOnClickListener {
                onBack()
            }


            pullToRefreshBtn.setOnClickListener {
                isPullToRefresh = !isPullToRefresh
                pullToRefreshIv.changeIcon(isPullToRefresh)
            }
            loaderScreenBtn.setOnClickListener {
                isLoaderScreen = !isLoaderScreen
                loaderScreenIV.changeIcon(isLoaderScreen)
            }
            fullScreenModeBtn.setOnClickListener {
                isFullScreenMode = !isFullScreenMode
                fullScreenModeIv.changeIcon(isFullScreenMode)
            }
            navigationButtonsBtn.setOnClickListener {
                isNavigationButtons = !isNavigationButtons
                navigationButtonsIv.changeIcon(isNavigationButtons)
            }
            externalLinksInBrowserBtn.setOnClickListener {
                isExternalLinksInBrowser = !isExternalLinksInBrowser
                externalLinksInBrowserIv.changeIcon(isExternalLinksInBrowser)
            }
            notificationSupportBtn.setOnClickListener {
                isNotificationSupport = !isNotificationSupport
                notificationSupportIv.changeIcon(isNotificationSupport)
            }

            nextBtn.setOnClickListener {

                val enabledFeatures = mutableListOf<String>()

                if (isPullToRefresh) enabledFeatures.add("Pull To Refresh")
                if (isLoaderScreen) enabledFeatures.add("Loader")
                if (isFullScreenMode) enabledFeatures.add("Full Screen")
                if (isNavigationButtons) enabledFeatures.add("Navigation")
                if (isExternalLinksInBrowser) enabledFeatures.add("External Links")
                if (isNotificationSupport) enabledFeatures.add("Notification")

                val enableFeatured = enabledFeatures.joinToString(",")


                val mIntent = Intent(this@AppFeaturesActivity, GenerateAppActivity::class.java).apply {
                    putExtra("webUrl", webUrl)
                    putExtra("appName", appName)
                    putExtra("packageName1", packageName1)
                    putExtra("appOrientation1", appOrientation1)
                    putExtra("primaryColor", primaryColor)
                    putExtra("secondaryColor", secondaryColor)
                    putExtra("imageUri", imageUri)
                    putExtra("enableFeatured", enableFeatured)

                    putExtra("isPullToRefresh", isPullToRefresh)
                    putExtra("isLoaderScreen", isLoaderScreen)
                    putExtra("isFullScreenMode", isFullScreenMode)
                    putExtra("isNavigationButtons", isNavigationButtons)
                    putExtra("isExternalLinksInBrowser", isExternalLinksInBrowser)
                    putExtra("isNotificationSupport", isNotificationSupport)
                }
                moveNext(mIntent)

            }

        }
    }



    fun moveNext(intent: Intent){
        startActivity(intent)
    }
}