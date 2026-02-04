package com.app.styletap.webtoappconverter.presentations.ui.activities.createApp

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.app.styletap.ads.NativeAdManager
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityBuildingAppBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.isNetworkAvailable
import com.app.styletap.webtoappconverter.presentations.ui.activities.home.MainActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.myApps.MyAppsActivity
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ACTION_FINISH_ACTIVITY
import com.app.styletap.webtoappconverter.presentations.utils.Contants.generateapp_native
import com.app.styletap.webtoappconverter.presentations.utils.Contants.myapps_native
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.storage
import java.io.File

class BuildingAppActivity : AppCompatActivity() {
    lateinit var binding: ActivityBuildingAppBinding
    var isClickable = false

    lateinit var prefHelper: PrefHelper

    var webUrl = ""
    var appName = ""
    var packageName1 = ""
    var appOrientation1 = ""

    var primaryColor = ""
    var secondaryColor = ""
    //var imageUri: Uri? = null
    var filePath: String? = ""

    var enableFeatured = ""

    var isPullToRefresh = true
    var isLoaderScreen = true
    var isFullScreenMode = true
    var isNavigationButtons = true
    var isExternalLinksInBrowser = true
    var isNotificationSupport = true

    var isEditMode = false
    var appId = ""
    var appVersion = "1"

    private lateinit var auth: FirebaseAuth
    var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeLocale()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityBuildingAppBinding.inflate(layoutInflater)
        setContentView(binding.root)
        customEnableEdgeToEdge()

        adjustTopHeight(binding.toolbarLL)
        adjustBottomHeight(binding.container)

        prefHelper = PrefHelper(this)
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

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
            filePath = it.getString("filePath", "")

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

            isEditMode = it.getBoolean("isEditMode", false)
            appId = it.getString("appId", "")
            appVersion = it.getString("appVersion", "1")

        }

        initView()
        showNativeAd()
    }

    fun onBack() {
        if (isClickable){
            finish()
        }
    }

    fun initView(){
        binding.apply {
            toolbar.titleTv.text = resources.getString(R.string.generate_app)
            /*toolbar.backBtn.isVisible = true
            toolbar.backBtn.setOnClickListener {
                onBack()
            }*/

            progressBar.progress = 25
            progressTv.text = "25"

            if (isEditMode){
                filePath?.let {
                    uploadAppIcon(it) { downloadUrl ->
                        progressBar.progress = 50
                        binding.progressTv.text = "50"
                        updateAppDetails(appId,downloadUrl)
                    }
                } ?: run{
                    progressBar.progress = 40
                    binding.progressTv.text = "40"
                    updateAppDetails(appId,null)

                }


            } else {

                uploadAppIcon(filePath!!) { downloadUrl ->
                    progressBar.progress = 50
                    binding.progressTv.text = "50"
                    if (downloadUrl != null) {
                        saveAppDetails(downloadUrl)
                    } else {
                        isClickable = true
                        Toast.makeText(this@BuildingAppActivity, resources.getString(R.string.failed_to_upload_app_icon), Toast.LENGTH_SHORT).show()
                        sendBroadcast(Intent(ACTION_FINISH_ACTIVITY).apply { setPackage(packageName) })
                        startActivity(Intent(this@BuildingAppActivity, MainActivity::class.java))
                        finish()
                    }
                }
            }

        }
    }


    /*fun uploadAppIcon(imageUri: Uri, onComplete: (downloadUrl: String?) -> Unit) {
        val storageRef = Firebase.storage.reference
        val timestamp = System.currentTimeMillis()
        val fileRef = storageRef.child("app_icons/app_icon_$timestamp.png")

        fileRef.putFile(imageUri)
            .addOnSuccessListener {
                // Get download URL
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    onComplete(uri.toString())
                }.addOnFailureListener {
                    onComplete(null)
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }*/

    fun uploadAppIcon(
        filePath: String,
        onComplete: (downloadUrl: String?) -> Unit
    ) {
        val storageRef = Firebase.storage.reference
        val timestamp = System.currentTimeMillis()
        val fileRef = storageRef.child("app_icons/app_icon_$timestamp.png")

        val file = File(filePath)
        if (!file.exists()) {
            onComplete(null)
            return
        }

        fileRef.putFile(file.toUri())
            .addOnSuccessListener {
                fileRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        onComplete(uri.toString())
                    }
                    .addOnFailureListener {
                        onComplete(null)
                    }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }


    fun saveAppDetails(
        appIconUrl: String
    ) {

        val db = FirebaseFirestore.getInstance()

        val _userId = auth.currentUser?.uid

        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        val userId: String
        val guestId: String

        if (currentUser.isAnonymous) {
            // Guest user
            guestId = currentUser.uid
            userId = "" // no real user
        } else {
            // Real user
            userId = currentUser.uid
            guestId = "" // no guest
        }


        fetchFcmToken(_userId) { fcmToken ->
            binding.progressBar.progress = 75
            binding.progressTv.text = "75"

            val uid = db.collection("apps").document().id

            // Complete data as a new record
            val appDetails = hashMapOf(
                "admobAppId" to "",
                "allowFileDownloads" to true,
                "apkUrl" to "",
                "appIconUrl" to appIconUrl,
                "appName" to appName,
                "appVersion" to "1",
                "bannerAdId" to "",
                "brandColor" to "#FFFFFF",
                "bundleUrl" to null,
                "category" to "",
                "createdAt" to System.currentTimeMillis(),
                "darkModeEnabled" to false,
                "description" to "",
                "enableAds" to false,
                "enableDesktopView" to false,
                "enableLoader" to isLoaderScreen,
                "enableOfflineCache" to false,
                "enablePullToRefresh" to isPullToRefresh,
                "enablePushNotifications" to isNotificationSupport,
                "errorMessage" to null,
                "fcmToken" to fcmToken,
                "fullScreenMode" to isFullScreenMode,
                "googleServicesUrl" to null,
                "guestId" to guestId,//"guest_$uid",
                "id" to uid,
                "interstitialAdId" to "",
                "navigationButtons" to isNavigationButtons,
                "openExternalInBrowser" to isExternalLinksInBrowser,
                "orientation" to appOrientation1.lowercase(),
                "packageName" to packageName1,
                "paidApp" to false,
                "primaryColor" to primaryColor,
                "processingStartedAt" to System.currentTimeMillis(),
                "secondaryColor" to secondaryColor,
                "showNoInternetAlert" to true,
                "splashScreenUrl" to null,
                "status" to "DRAFT",
                "updatedAt" to System.currentTimeMillis(),
                "urlValidationEnabled" to true,
                "userId" to userId,
                "websiteUrl" to webUrl
            )

        /*binding.progressBar.progress = 75
        binding.progressTv.text = "75"*/

            db.collection("apps")
                .document(uid)  // use generated UID
                .set(appDetails)
                .addOnSuccessListener {
                    isClickable = true
                    binding.progressBar.progress = 100
                    binding.progressTv.text = "100"
                    Toast.makeText(this, resources.getString(R.string.app_details_saved_successfully), Toast.LENGTH_SHORT).show()
                    sendBroadcast(Intent(ACTION_FINISH_ACTIVITY).apply { setPackage(packageName) })
                    startActivity(Intent(this@BuildingAppActivity, MyAppsActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    isClickable = true
                    binding.progressBar.progress = 100
                    binding.progressTv.text = "100"
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    sendBroadcast(Intent(ACTION_FINISH_ACTIVITY).apply { setPackage(packageName) })
                    startActivity(Intent(this@BuildingAppActivity, MainActivity::class.java))
                    finish()
                }
        }
    }



    fun updateAppDetails(
        appId: String,
        appIconUrl: String? = null
    ) {

        if (appId.isEmpty()){
            finish()
            return
        }

        val db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid

        fetchFcmToken(userId) { fcmToken ->
            binding.progressBar.progress = 75
            binding.progressTv.text = "75"

            // Complete data as a new record
            val appDetails = hashMapOf<String, Any?>(
                "allowFileDownloads" to true,
                "apkUrl" to "",
                "appName" to appName,
                "appVersion" to appVersion,
                "brandColor" to "#FFFFFF",
                "bundleUrl" to null,
                "darkModeEnabled" to false,
                "enableDesktopView" to false,
                "enableLoader" to isLoaderScreen,
                "enableOfflineCache" to false,
                "enablePullToRefresh" to isPullToRefresh,
                "enablePushNotifications" to isNotificationSupport,
                "errorMessage" to null,
                "fcmToken" to fcmToken,
                "fullScreenMode" to isFullScreenMode,
                "id" to appId,
                "navigationButtons" to isNavigationButtons,
                "openExternalInBrowser" to isExternalLinksInBrowser,
                "orientation" to appOrientation1.lowercase(),
                "packageName" to packageName1,
                "paidApp" to false,
                "primaryColor" to primaryColor,
                "processingStartedAt" to System.currentTimeMillis(),
                "secondaryColor" to secondaryColor,
                "showNoInternetAlert" to true,
                "splashScreenUrl" to null,
                "status" to "DRAFT",
                "updatedAt" to System.currentTimeMillis(),
                "urlValidationEnabled" to true,
                "userId" to userId,
                "websiteUrl" to webUrl
            )

            appIconUrl?.let {
                appDetails["appIconUrl"] = it
            }

        /*binding.progressBar.progress = 75
        binding.progressTv.text = "75"*/

            db.collection("apps")
                .document(appId)  // use generated UID
                .update(appDetails)
                .addOnSuccessListener {
                    isClickable = true
                    binding.progressBar.progress = 100
                    binding.progressTv.text = "100"
                    Toast.makeText(this, resources.getString(R.string.app_details_saved_successfully), Toast.LENGTH_SHORT).show()
                    sendBroadcast(Intent(ACTION_FINISH_ACTIVITY).apply { setPackage(packageName) })
                    startActivity(Intent(this@BuildingAppActivity, MyAppsActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    isClickable = true
                    binding.progressBar.progress = 100
                    binding.progressTv.text = "100"
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    sendBroadcast(Intent(ACTION_FINISH_ACTIVITY).apply { setPackage(packageName) })
                    startActivity(Intent(this@BuildingAppActivity, MainActivity::class.java))
                    finish()
                }
        }
    }



    private fun fetchFcmToken(
        userId: String?,
        callback: (String) -> Unit
    ) {
        if (userId == null) {
            callback("")
            return
        }
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { snapshot ->

                val storedToken = snapshot.getString("fcmToken")

                if (!storedToken.isNullOrBlank()) {
                    // ✅ Token exists in DB
                    callback(storedToken)
                } else {
                    // 🔁 Token missing → generate new
                    FirebaseMessaging.getInstance().token
                        .addOnSuccessListener { newToken ->

                            // save it back to Firestore
                            db.collection("users")
                                .document(userId)
                                .update("fcmToken", newToken)

                            callback(newToken)
                        }
                }
            }
            .addOnFailureListener {
                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token ->
                        callback(token)
                    }
            }
    }



    fun showNativeAd(){
        if (isNetworkAvailable() && prefHelper.getBooleanDefultTrue(generateapp_native) && !prefHelper.getIsPurchased()){
            binding.adParentLayout.visibility = View.VISIBLE
            binding.nativeLayout.visibility = View.VISIBLE
            binding.shimmerContainer.nativeShimmerView.startShimmer()
            binding.shimmerContainer.nativeShimmerView.visibility = View.VISIBLE
            NativeAdManager(this).loadAndPopulateNativeAdView(this,resources.getString(R.string.generateAppScreenNativeId),binding.adFrame, R.layout.native_ad_medium, binding.shimmerContainer.nativeShimmerView)
        } else {
            binding.adParentLayout.visibility = View.GONE
            binding.shimmerContainer.nativeShimmerView.stopShimmer()
        }
    }
}