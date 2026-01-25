package com.app.styletap.webtoappconverter.presentations.ui.activities.myApps


import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityMyAppsBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.downloadFile
import com.app.styletap.webtoappconverter.models.AppModel
import com.app.styletap.webtoappconverter.presentations.ui.activities.myApps.adapters.MyAppsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.app.styletap.webtoappconverter.extentions.hasStoragePermission
import com.app.styletap.webtoappconverter.extentions.showStorageRationaleDialog
import com.app.styletap.webtoappconverter.extentions.toMillis
import com.app.styletap.webtoappconverter.presentations.ui.activities.home.MainActivity
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ACTION_FINISH_ACTIVITY
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ACTION_REFRESH_ACTIVITY
import com.app.styletap.webtoappconverter.presentations.utils.Contants.DRAFT_BUNDLE
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query

class MyAppsActivity : AppCompatActivity() {
    lateinit var binding: ActivityMyAppsBinding

    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                pendingDownload?.let {
                    downloadFile(it.first, it.second, false)
                }
            } else {
                Toast.makeText(this, resources.getString(R.string.storage_permission_is_required_to_save_the_file), Toast.LENGTH_LONG).show()
            }
        }

    private var pendingDownload: Pair<String, String>? = null


    private val finishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_FINISH_ACTIVITY) {
                finish()
            }
        }
    }


    var isClickable = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeLocale()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityMyAppsBinding.inflate(layoutInflater)
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
        if (isClickable){
            finish()
        }
    }

    fun initView() {
        binding.apply {
            toolbar.titleTv.text = resources.getString(R.string.my_apps)
            toolbar.backBtn.isVisible = true
            toolbar.backBtn.setOnClickListener {
                onBack()
            }

            reFetchBtn.setOnClickListener {
                fetchMyApps()
            }

        }

        fetchMyApps()
    }

    fun fetchMyApps(){
        isClickable = false

        binding.reFetchBtn.isVisible = false
        binding.progressBar.isVisible = true
        binding.emptyMsgTv.isVisible = false

        binding.recyclerView.layoutManager = LinearLayoutManager(this)



        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("apps")
            .where(
                Filter.or(
                Filter.equalTo("userId", userId),
                Filter.equalTo("guestId", userId)
            ))
            .get()
            .addOnSuccessListener { snapshot ->
                //val appList = snapshot.documents.mapNotNull { it.toObject(AppModel::class.java) }

                val appList = snapshot.documents
                    .mapNotNull { it.toObject(AppModel::class.java) }
                    .sortedByDescending { app ->
                        app.updatedAt.toMillis() ?: 0L
                    }

                isClickable = true

                binding.reFetchBtn.isVisible = true

                if (appList.isEmpty()){
                    binding.emptyMsgTv.isVisible = true
                    binding.recyclerView.isVisible = false
                    binding.progressBar.isVisible = false

                } else {
                    binding.emptyMsgTv.isVisible = false
                    binding.recyclerView.isVisible = true
                    binding.progressBar.isVisible = false

                    val adapter = MyAppsAdapter(this,appList) { app ->

                    }

                    binding.recyclerView.adapter = adapter
                }

            }
            .addOnFailureListener {
                isClickable = true
                binding.progressBar.isVisible = false
                binding.emptyMsgTv.isVisible = true
                binding.reFetchBtn.isVisible = false
                binding.recyclerView.isVisible = false
            }

    }

    fun deleteApp(appId: String){
        binding.progressBar.isVisible = true
        val db = FirebaseFirestore.getInstance()

        db.collection("apps")
            .document(appId)
            .delete()
            .addOnSuccessListener {
                fetchMyApps()
                Toast.makeText(this, resources.getString(R.string.app_deleted), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                binding.progressBar.isVisible = false
                Toast.makeText(this, "${resources.getString(R.string.failed_to_delete)}: ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }




    fun startDownload(url: String, appName: String) {
        if (hasStoragePermission()) {
            downloadFile(url, appName, false)
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
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }


    fun generateBundle(
        appId: String,
        onRequestSubmission: () -> Unit
    ) {
        if (appId.isEmpty()){
            onRequestSubmission()
            return
        }

        isClickable = true
        binding.reFetchBtn.isVisible = false
        binding.progressBar.isVisible = true

        val db = FirebaseFirestore.getInstance()

        val appDetails = hashMapOf<String, Any?>(
            "status" to DRAFT_BUNDLE,
            )

        db.collection("apps")
            .document(appId)  // use generated UID
            .update(appDetails)
            .addOnSuccessListener {
                onRequestSubmission()
                isClickable = true
                fetchMyApps()
                Toast.makeText(this, resources.getString(R.string.bundle_request_submitted), Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener { e ->
                onRequestSubmission()
                isClickable = true
                binding.reFetchBtn.isVisible = true
                binding.progressBar.isVisible = false
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



}