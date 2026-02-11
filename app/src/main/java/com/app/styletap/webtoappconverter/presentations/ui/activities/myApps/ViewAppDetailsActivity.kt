package com.app.styletap.webtoappconverter.presentations.ui.activities.myApps

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import com.app.styletap.interfaces.FirebaseAnalyticsUtils
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityViewAppDetailsBinding
import com.app.styletap.webtoappconverter.extentions.addDynamicChips
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.decorateStatus
import com.app.styletap.webtoappconverter.extentions.downloadFile
import com.app.styletap.webtoappconverter.extentions.hasStoragePermission
import com.app.styletap.webtoappconverter.extentions.isNetworkAvailable
import com.app.styletap.webtoappconverter.extentions.showStorageRationaleDialog
import com.app.styletap.webtoappconverter.extentions.toFormattedDate
import com.app.styletap.webtoappconverter.extentions.toMillis
import com.app.styletap.webtoappconverter.models.AppModel
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ACTION_FINISH_ACTIVITY
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ACTION_REFRESH_ACTIVITY
import com.app.styletap.webtoappconverter.presentations.utils.Contants.DRAFT
import com.app.styletap.webtoappconverter.presentations.utils.Contants.READY_TO_DOWNLOAD
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class ViewAppDetailsActivity : AppCompatActivity() {
    lateinit var binding: ActivityViewAppDetailsBinding

    private val finishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_FINISH_ACTIVITY) {
                finish()
            }
        }
    }

    var appId = ""

    var appModel: AppModel? = null
    var isApk = true

    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                pendingDownload?.let {
                    downloadFile(it.first, it.second,isShareFile, isApk)
                }
            } else {
                Toast.makeText(this, resources.getString(R.string.storage_permission_is_required_to_save_the_file), Toast.LENGTH_LONG).show()
            }
        }

    private var pendingDownload: Pair<String, String>? = null

    var isShareFile = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeLocale()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityViewAppDetailsBinding.inflate(layoutInflater)
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
            toolbar.titleTv.text = resources.getString(R.string.app_details)
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

                            appNameTv.text = app.appName ?: ""
                            webUrlTv.text = app.websiteUrl ?: ""

                            val millis = app.updatedAt.toMillis()
                            dateTv.text =  "${millis?.toFormattedDate()}"

                            versionTv.text = "V: " + app.appVersion ?: "1"

                            statusTv.decorateStatus(statusTv.context,app.status ?: DRAFT)

                            app.primaryColor?.let { primaryColor ->
                                primaryColorTv.text = primaryColor
                                primaryColorCD.setCardBackgroundColor(ColorStateList.valueOf(primaryColor.toColorInt()))
                            }

                            app.secondaryColor?.let { secondaryColor ->
                                secondaryColorTv.text = secondaryColor
                                secondaryColorCD.setCardBackgroundColor(ColorStateList.valueOf(secondaryColor.toColorInt()))
                            }


                            val enabledFeatures = arrayListOf<String>().apply {
                                if (app.enablePullToRefresh) add("Pull To Refresh")
                                if (app.enableLoader) add("Loader")
                                if (app.fullScreenMode) add("Full Screen")
                                if (app.navigationButtons) add("Navigation")
                                if (app.openExternalInBrowser) add("External Links")
                                if (app.enablePushNotifications) add("Notification")
                            }

                            if (enabledFeatures.isNotEmpty()){
                                featuresCard.isVisible = true
                                addDynamicChips( chipGroup, enabledFeatures)
                            }

                            if (app.status == READY_TO_DOWNLOAD){
                                editAndRebuildBtn.isVisible = true
                                shareBtn.isVisible = true
                                downloadBtn.isVisible = true
                            } else {
                                editAndRebuildBtn.isVisible = false
                                shareBtn.isVisible = false
                                downloadBtn.isVisible = false
                            }

                            editAndRebuildBtn.setOnClickListener {
                                app.id?.let { appId ->
                                    if (!isNetworkAvailable()){
                                        Toast.makeText(this@ViewAppDetailsActivity, resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                                    } else {
                                        val mIntent = Intent(this@ViewAppDetailsActivity, EditAppActivity::class.java).apply {
                                            putExtra("appId", appId)
                                        }
                                        startActivity(mIntent)
                                    }
                                }
                            }

                            shareBtn.setOnClickListener {
                                isShareFile = true
                                isApk = false
                                app.apkUrl?.let { appUrl->
                                    if (!isNetworkAvailable()){
                                        Toast.makeText(this@ViewAppDetailsActivity, resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                                    } else {
                                        startDownload(appUrl, app.appName ?: "app")
                                    }
                                }
                            }

                            downloadBtn.setOnClickListener {
                                isShareFile = false
                                isApk = true
                                app.apkUrl?.let { appUrl->
                                    if (!isNetworkAvailable()){
                                        Toast.makeText(this@ViewAppDetailsActivity, resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                                    } else {
                                        startDownload(appUrl, app.appName ?: "app")
                                    }
                                }
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

    fun startDownload(url: String, appName: String) {
        if (hasStoragePermission()) {
            downloadFile(url, appName, isShareFile, isApk)
            return
        }

        pendingDownload = Pair(url, appName)

        // Should we show rationale?
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            showStorageRationaleDialog(storagePermissionLauncher)
        } else {
            // First time or "Don't ask again"
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

}