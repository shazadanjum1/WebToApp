package com.app.styletap.webtoappconverter.presentations.ui.activities.myApps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.app.styletap.ads.InterstitialAdManager
import com.app.styletap.interfaces.FirebaseAnalyticsUtils
import com.app.styletap.interfaces.InterstitialLoadCallback
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityEditAppBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.changeIcon
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.incrementVersion
import com.app.styletap.webtoappconverter.extentions.isNetworkAvailable
import com.app.styletap.webtoappconverter.extentions.isValidPackageName
import com.app.styletap.webtoappconverter.extentions.showColorPicker
import com.app.styletap.webtoappconverter.extentions.uriToTempFile
import com.app.styletap.webtoappconverter.models.AppModel
import com.app.styletap.webtoappconverter.presentations.ui.activities.createApp.BuildingAppActivity
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ACTION_FINISH_ACTIVITY
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ACTION_REFRESH_ACTIVITY
import com.app.styletap.webtoappconverter.presentations.utils.Contants.buildapp_inter
import com.app.styletap.webtoappconverter.presentations.utils.Contants.isIntertialAdshowing
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class EditAppActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditAppBinding
    var appId = ""
    var appModel: AppModel? = null

    var isPullToRefresh = true
    var isLoaderScreen = true
    var isFullScreenMode = false
    var isNavigationButtons = true
    var isExternalLinksInBrowser = true
    var isNotificationSupport = false

    //private var imageUri: Uri? = null
    var filePath: String? = ""

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {

            val file = uriToTempFile( uri)
            filePath = file.absolutePath
            //imageUri = uri

            Glide.with(this)
                .load(file)
                .circleCrop()
                .into(binding.appIconIv)

        }
    }

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
        binding = ActivityEditAppBinding.inflate(layoutInflater)
        setContentView(binding.root)
        customEnableEdgeToEdge()

        adjustTopHeight(binding.toolbarLL)
        //adjustBottomHeight(binding.container)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())

            val bottomInset = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            ).bottom

            v.updatePadding(bottom = bottomInset)

            insets
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBack()
                }
            })

        intent.extras?.let {
            appId = it.getString("appId", "")
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

    fun initView(){
        binding.apply {
            toolbar.titleTv.text = resources.getString(R.string.edit_app)
            toolbar.backBtn.isVisible = true
            toolbar.backBtn.setOnClickListener {
                onBack()
            }


        }

        getApp()
    }

    fun getApp(){
        if (appId.isEmpty()){
            finish()
            return
        }

        binding.progressBar.isVisible = true

        val db = FirebaseFirestore.getInstance()

        db.collection("apps")
            .document(appId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.progressBar.isVisible = false
                    appModel = document.toObject(AppModel::class.java)

                    binding.apply {
                        appModel?.let { app ->
                            app.appIconUrl?.let {
                                Glide.with(applicationContext)
                                    .load(it)
                                    .circleCrop()
                                    .into(appIconIv)
                            }

                            etAppName.setText(app.appName ?: "")
                            etUrl.setText(app.websiteUrl ?: "")
                            etPackageName.setText(app.packageName ?: "")

                            app.primaryColor?.let { primaryColor ->
                                primaryColorTv.text = primaryColor
                                primaryColorCD.setCardBackgroundColor(ColorStateList.valueOf(primaryColor.toColorInt()))
                            }

                            app.secondaryColor?.let { secondaryColor ->
                                secondaryColorTv.text = secondaryColor
                                secondaryColorCD.setCardBackgroundColor(ColorStateList.valueOf(secondaryColor.toColorInt()))
                            }

                            isPullToRefresh = app.enablePullToRefresh
                            isLoaderScreen = app.enableLoader
                            isFullScreenMode = app.fullScreenMode
                            isNavigationButtons = app.navigationButtons
                            isExternalLinksInBrowser = app.openExternalInBrowser
                            isNotificationSupport = app.enablePushNotifications

                            pullToRefreshIv.changeIcon(isPullToRefresh)
                            loaderScreenIV.changeIcon(isLoaderScreen)
                            fullScreenModeIv.changeIcon(isFullScreenMode)
                            navigationButtonsIv.changeIcon(isNavigationButtons)
                            externalLinksInBrowserIv.changeIcon(isExternalLinksInBrowser)
                            notificationSupportIv.changeIcon(isNotificationSupport)


                            primaryColorBtn.setOnClickListener {
                                showColorPicker() { selectedColor ->
                                    changeColor(selectedColor, "primaryColor")
                                }
                            }
                            secondaryColorBtn.setOnClickListener {
                                showColorPicker() { selectedColor ->
                                    changeColor(selectedColor, "secondaryColor")
                                }
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

                            uploadImageBtn.setOnClickListener {
                                pickImageLauncher.launch("image/*")
                            }


                            updateBtn.setOnClickListener {
                                validateAndUpdateApp()
                            }



                        }
                    }

                } else {
                    sendBroadcast(Intent(ACTION_REFRESH_ACTIVITY).apply { setPackage(packageName) })
                    finish()
                }
            }
            .addOnFailureListener { e ->
                FirebaseAnalyticsUtils.logEventMessage("server_error")
                Toast.makeText(this, resources.getString(R.string.something_went_wrong_try_again), Toast.LENGTH_LONG).show()
                finish()
            }


    }


    private fun changeColor(selectedColor: Int,  useFor: String) {
        val hexColor = String.format("#%06X", 0xFFFFFF and selectedColor)

        if (useFor == "secondaryColor"){
            binding.secondaryColorTv.text = hexColor
            binding.secondaryColorCD.setCardBackgroundColor(ColorStateList.valueOf(selectedColor))
        }  else{
            binding.primaryColorTv.text = hexColor
            binding.primaryColorCD.setCardBackgroundColor(ColorStateList.valueOf(selectedColor))
        }
    }

    fun validateAndUpdateApp(){

        binding.apply {
            val url = etUrl.text.toString().trim()
            if (!invalidateUrl(url)) return@validateAndUpdateApp

            val appName = etAppName.text.toString().trim()
            val packageName1 = etPackageName.text.toString().trim()

            if (!invalidateInputs(appName, packageName1)) return@validateAndUpdateApp

            val enabledFeatures = mutableListOf<String>()

            if (isPullToRefresh) enabledFeatures.add("Pull To Refresh")
            if (isLoaderScreen) enabledFeatures.add("Loader")
            if (isFullScreenMode) enabledFeatures.add("Full Screen")
            if (isNavigationButtons) enabledFeatures.add("Navigation")
            if (isExternalLinksInBrowser) enabledFeatures.add("External Links")
            if (isNotificationSupport) enabledFeatures.add("Notification")

            val enableFeatured = enabledFeatures.joinToString(",")

            if (!isNetworkAvailable()){
                Toast.makeText(this@EditAppActivity, resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                return@validateAndUpdateApp
            }


            /*val currentVersion = appModel?.appVersion ?: "1.0"
            val nextVersion = incrementVersion(currentVersion)*/

            val currentVersion = appModel?.appVersion ?: "1"
            val nextVersion = (currentVersion.toIntOrNull() ?: 1) + 1

            val mIntent = Intent(this@EditAppActivity, BuildingAppActivity::class.java).apply {
                putExtra("webUrl", url)
                putExtra("appName", appName)
                //putExtra("packageName1", packageName1)
                putExtra("packageName1", isValidPackageName(packageName1))

                putExtra("appOrientation1", appModel?.orientation ?: "Portrait")
                putExtra("primaryColor", primaryColorTv.text)
                putExtra("secondaryColor", secondaryColorTv.text)
                putExtra("filePath", filePath)
                putExtra("enableFeatured", enableFeatured)

                putExtra("isPullToRefresh", isPullToRefresh)
                putExtra("isLoaderScreen", isLoaderScreen)
                putExtra("isFullScreenMode", isFullScreenMode)
                putExtra("isNavigationButtons", isNavigationButtons)
                putExtra("isExternalLinksInBrowser", isExternalLinksInBrowser)
                putExtra("isNotificationSupport", isNotificationSupport)

                putExtra("isEditMode", true)
                putExtra("appId", appId)
                putExtra("appVersion", nextVersion.toString())

            }
            moveNext(mIntent)

        }


    }

    fun moveNext(intent: Intent){

        if (PrefHelper.getIsPurchased() || !PrefHelper.getBooleanDefultTrue(buildapp_inter)){
            startActivity(intent)
        } else {
            isIntertialAdshowing = true
            InterstitialAdManager(this).loadAndShowAd(
                getString(R.string.buildAppInterstitialId),
                PrefHelper.getBooleanDefultTrue(buildapp_inter),
                object : InterstitialLoadCallback{
                    override fun onFailedToLoad() {
                        isIntertialAdshowing = false
                        Toast.makeText(this@EditAppActivity, resources.getString(R.string.failed_to_load_ads_please_try_again), Toast.LENGTH_SHORT).show()
                    }
                    override fun onLoaded() {
                        isIntertialAdshowing = false
                        startActivity(intent)
                    }
                }
            )
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


    fun invalidateInputs(appName: String, packageName1: String): Boolean {
        binding.apply {

            if (appName.isEmpty() || appName.length < 3) {
                etAppName.apply {
                    requestFocus()
                    error = context.getString(R.string.app_name_must_be_at_least_3_letters_and_contain_only_alphabets)
                }
                return false
            }

            /*if (!isValidPackageName(packageName1)) {
                etPackageName.apply {
                    requestFocus()
                    error = context.getString(R.string.the_package_name_must_follow_the_format_com_example_app)
                }
                return false
            }*/

            return true
        }

    }



}