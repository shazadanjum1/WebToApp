package com.app.styletap.webtoappconverter.presentations.ui.activities.tutorials

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityTutorialsBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.presentations.utils.Contants.CREATE_APP
import com.app.styletap.webtoappconverter.presentations.utils.Contants.CUSTOM_DOMAIN
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ENABLE_MONITIZATION
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ENABLE_NOTIFICATIONS
import com.app.styletap.webtoappconverter.presentations.utils.Contants.UPLOAD_APP

class TutorialsActivity : AppCompatActivity() {
    lateinit var binding: ActivityTutorialsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeLocale()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityTutorialsBinding.inflate(layoutInflater)
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

    }

    fun onBack() {
        finish()
    }

    fun initView(){
        binding.apply {
            toolbar.titleTv.text = resources.getString(R.string.tutorials)
            toolbar.backBtn.isVisible = true
            toolbar.backBtn.setOnClickListener {
                onBack()
            }

            createAppBtn.setOnClickListener {
                moveNext(CREATE_APP)
            }


            uploadAppBtn.setOnClickListener {
                moveNext(UPLOAD_APP)
            }

            enableNotificationBtn.setOnClickListener {
                moveNext(ENABLE_NOTIFICATIONS)
            }

            customDomainBtn.setOnClickListener {
                moveNext(CUSTOM_DOMAIN)
            }

            admobBtn.setOnClickListener {
                moveNext(ENABLE_MONITIZATION)
            }

        }
    }

    fun moveNext(serviceType: String){
        val mIntent = Intent(this@TutorialsActivity, TutorialDetailsActivity::class.java).apply {
            putExtra("serviceType", serviceType)
        }
        startActivity(mIntent)
    }
}