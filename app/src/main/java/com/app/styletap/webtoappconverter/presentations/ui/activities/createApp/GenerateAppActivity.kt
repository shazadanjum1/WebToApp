package com.app.styletap.webtoappconverter.presentations.ui.activities.createApp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityGenerateAppBinding
import com.app.styletap.webtoappconverter.extentions.addDynamicChips
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.isNetworkAvailable
import androidx.core.graphics.toColorInt
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ACTION_FINISH_ACTIVITY

class GenerateAppActivity : AppCompatActivity() {
    lateinit var binding: ActivityGenerateAppBinding

    var webUrl = ""
    var appName = ""
    var packageName1 = ""
    var appOrientation1 = ""

    var primaryColor = ""
    var secondaryColor = ""
    //var imageUri: Uri? = null
    var filePath: String? = ""

    var isPullToRefresh = true
    var isLoaderScreen = true
    var isFullScreenMode = true
    var isNavigationButtons = true
    var isExternalLinksInBrowser = true
    var isNotificationSupport = true

    var enableFeatured = ""

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
        binding = ActivityGenerateAppBinding.inflate(layoutInflater)
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

            filePath = it.getString("filePath", filePath)

            /*imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("imageUri", Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<Uri>("imageUri")
            }*/

            enableFeatured = it.getString("enableFeatured", "")

            isPullToRefresh = it.getBoolean("isPullToRefresh", true)
            isLoaderScreen = it.getBoolean("isLoaderScreen", true)
            isFullScreenMode = it.getBoolean("isFullScreenMode", true)
            isNavigationButtons = it.getBoolean("isNavigationButtons", true)
            isExternalLinksInBrowser = it.getBoolean("isExternalLinksInBrowser", true)
            isNotificationSupport = it.getBoolean("isNotificationSupport", true)

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
            toolbar.titleTv.text = resources.getString(R.string.generate_app)
            toolbar.backBtn.isVisible = true
            toolbar.backBtn.setOnClickListener {
                onBack()
            }

            urlTv.text = webUrl
            appNameTv.text = appName
            orientationTv.text = appOrientation1
            primaryColorTv.text = primaryColor
            secondaryColorTv.text = secondaryColor

            primaryColorCD.setCardBackgroundColor(ColorStateList.valueOf(primaryColor.toColorInt()))
            secondaryColorCD.setCardBackgroundColor(ColorStateList.valueOf(secondaryColor.toColorInt()))


            if (enableFeatured.isNotEmpty()){
                val chipList = ArrayList(enableFeatured.split(","))//arrayListOf("Android", "Kotlin", "Jetpack", "Compose")
                if (chipList.isNotEmpty()){
                    enabledFeaturesTag.isVisible = true
                    chipGroup.isVisible = true
                    addDynamicChips( chipGroup, chipList)
                }
            }

            nextBtn.setOnClickListener {

                if (!isNetworkAvailable()){
                    Toast.makeText(this@GenerateAppActivity, resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val mIntent = Intent(this@GenerateAppActivity, BuildingAppActivity::class.java).apply {
                    putExtra("webUrl", webUrl)
                    putExtra("appName", appName)
                    putExtra("packageName1", packageName1)
                    putExtra("appOrientation1", appOrientation1)
                    putExtra("primaryColor", primaryColor)
                    putExtra("secondaryColor", secondaryColor)
                    putExtra("filePath", filePath)
                    putExtra("enableFeatured", enableFeatured)

                    putExtra("isPullToRefresh", isPullToRefresh)
                    putExtra("isLoaderScreen", isLoaderScreen)
                    putExtra("isFullScreenMode", isFullScreenMode)
                    putExtra("isNavigationButtons", isNavigationButtons)
                    putExtra("isExternalLinksInBrowser", isExternalLinksInBrowser)
                    putExtra("isNotificationSupport", isNotificationSupport)

                    putExtra("isEditMode", false)

                }
                moveNext(mIntent)
            }

        }
    }

    fun moveNext(intent: Intent){
        startActivity(intent)
    }
}