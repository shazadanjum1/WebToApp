package com.app.styletap.webtoappconverter.presentations.ui.activities.services

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityServicesBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ADMOB_MONETIZATION
import com.app.styletap.webtoappconverter.presentations.utils.Contants.CUSTOM_APP
import com.app.styletap.webtoappconverter.presentations.utils.Contants.PLAY_STORE
import com.app.styletap.webtoappconverter.presentations.utils.Contants.PREMIUM_BUILD
import com.app.styletap.webtoappconverter.presentations.utils.Contants.PUSH_NOTIFICATIONS

class ServicesActivity : AppCompatActivity() {
    lateinit var binding: ActivityServicesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeLocale()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityServicesBinding.inflate(layoutInflater)
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
            toolbar.titleTv.text = resources.getString(R.string.services)
            toolbar.backBtn.isVisible = true
            toolbar.backBtn.setOnClickListener {
                onBack()
            }

            publishAppBtn.setOnClickListener {
                moveNext(PLAY_STORE)
            }

            customAppBtn.setOnClickListener {
                moveNext(CUSTOM_APP)
            }

            notificationBtn.setOnClickListener {
                moveNext(PUSH_NOTIFICATIONS)
            }

            admobBtn.setOnClickListener {
                moveNext(ADMOB_MONETIZATION)
            }

            proBuildBtn.setOnClickListener {
                moveNext(PREMIUM_BUILD)
            }

        }
    }

    fun moveNext(serviceType: String){
        val mIntent = Intent(this@ServicesActivity, ServiceDetailsActivity::class.java).apply {
            putExtra("serviceType", serviceType)
        }
        startActivity(mIntent)
    }
}