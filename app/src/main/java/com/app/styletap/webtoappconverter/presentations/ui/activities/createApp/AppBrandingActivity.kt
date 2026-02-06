package com.app.styletap.webtoappconverter.presentations.ui.activities.createApp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.app.styletap.webtoappconverter.MyApplication
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityAppBrandingBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.isNetworkAvailable
import com.app.styletap.webtoappconverter.extentions.showColorPicker
import com.app.styletap.webtoappconverter.extentions.uriToTempFile
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ACTION_FINISH_ACTIVITY
import com.app.styletap.webtoappconverter.presentations.utils.Contants.createapp_native
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper
import com.bumptech.glide.Glide
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder

class AppBrandingActivity : AppCompatActivity() {
    lateinit var binding: ActivityAppBrandingBinding

    var webUrl = ""
    var appName = ""
    var packageName1 = ""
    var appOrientation1 = ""


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
                .into(binding.appIconImageView)

            binding.appIconImageView.isVisible = true
            binding.iconErrorTv.isVisible = false
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
        binding = ActivityAppBrandingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        customEnableEdgeToEdge()

        adjustTopHeight(binding.toolbarLL)
        adjustBottomHeight(binding.container)

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

        intent.extras?.let {
            webUrl = it.getString("webUrl", "")
            appName = it.getString("appName", "")
            packageName1 = it.getString("packageName1", "")
            appOrientation1 = it.getString("appOrientation1", "")
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
            toolbar.titleTv.text = resources.getString(R.string.app_branding)
            toolbar.backBtn.isVisible = true
            toolbar.backBtn.setOnClickListener {
                onBack()
            }


            uploadIconBtn.setOnClickListener {
                pickImageLauncher.launch("image/*")
            }

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

            nextBtn.setOnClickListener {

                if (filePath.isNullOrEmpty()){
                    binding.iconErrorTv.isVisible = true
                    return@setOnClickListener
                }

                val mIntent = Intent(this@AppBrandingActivity, AppFeaturesActivity::class.java).apply {
                    putExtra("webUrl", webUrl)
                    putExtra("appName", appName)
                    putExtra("packageName1", packageName1)
                    putExtra("appOrientation1", appOrientation1)
                    putExtra("primaryColor", primaryColorTv.text)
                    putExtra("secondaryColor", secondaryColorTv.text)
                    putExtra("filePath", filePath)

                }
                moveNext(mIntent)
            }

        }
    }

    fun moveNext(intent: Intent){
        startActivity(intent)
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

}