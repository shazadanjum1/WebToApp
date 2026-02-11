package com.app.styletap.webtoappconverter.presentations.ui.activities.services

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.app.styletap.interfaces.FirebaseAnalyticsUtils
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityServiceDetailsBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.openWhatsApp
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ADMOB_MONETIZATION
import com.app.styletap.webtoappconverter.presentations.utils.Contants.CUSTOM_APP
import com.app.styletap.webtoappconverter.presentations.utils.Contants.PLAY_STORE
import com.app.styletap.webtoappconverter.presentations.utils.Contants.PREMIUM_BUILD
import com.app.styletap.webtoappconverter.presentations.utils.Contants.PUSH_NOTIFICATIONS
import com.bumptech.glide.Glide

class ServiceDetailsActivity : AppCompatActivity() {
    lateinit var binding: ActivityServiceDetailsBinding

    var serviceType: String = PLAY_STORE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeLocale()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityServiceDetailsBinding.inflate(layoutInflater)
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
            serviceType = it.getString("serviceType", PLAY_STORE)
        }

        initView()

    }

    fun onBack() {
        finish()
    }

    fun initView(){
        binding.apply {
            toolbar.titleTv.text = resources.getString(R.string.service_details)
            toolbar.backBtn.isVisible = true
            toolbar.backBtn.setOnClickListener {
                onBack()
            }


            if (serviceType == CUSTOM_APP){
                setCustomAppTypeData()
            } else if (serviceType == PUSH_NOTIFICATIONS){
                setNotificationTypeData()
            } else if (serviceType == ADMOB_MONETIZATION){
                setAdmobTypeData()
            } else if (serviceType == PREMIUM_BUILD){
                setProBuildTypeData()
            } else {
                setPlayStoreTypeData()
            }


            orderBtn.setOnClickListener {
                FirebaseAnalyticsUtils.logEventMessage("agency_whatsapp_click")
                openWhatsApp(getString(R.string.phone_number))
            }
        }
    }


    fun setPlayStoreTypeData(){
        binding.apply {
            appTitle.text = resources.getString(R.string.publish_app_on_google_play)
            appMsg.text = resources.getString(R.string.we_will_publish_your_app_on_google_play_store)

            f1.text = resources.getString(R.string.app_store_optimization)
            f2.text = resources.getString(R.string.professional_results)
            f3.text = resources.getString(R.string.app_description_writing)
            f4.text = resources.getString(R.string.category_selection)
            f5.text = resources.getString(R.string.privacy_policy_creation)

            loadIcon(R.drawable.ic_publish_app)
        }
    }

    fun setCustomAppTypeData(){
        binding.apply {
            appTitle.text = resources.getString(R.string.custom_android_app_development)
            appMsg.text = resources.getString(R.string.get_a_fully_customized_android_app_built_for_you)

            f1.text = resources.getString(R.string.custom_uiux_design)
            f2.text = resources.getString(R.string.native_android_development)
            f3.text = resources.getString(R.string.api_integration)
            f4.text = resources.getString(R.string.database_setup)
            f5.text = resources.getString(R.string.ongoing_support)

            loadIcon(R.drawable.ic_custom_app)

        }
    }

    fun setNotificationTypeData(){
        binding.apply {
            appTitle.text = resources.getString(R.string.push_notifications_setup)
            appMsg.text = resources.getString(R.string.enable_push_notifications_for_your_app)

            f1.text = resources.getString(R.string.firebase_setup)
            f2.text = resources.getString(R.string.notification_templates)
            f3.text = resources.getString(R.string.scheduling_support)
            f4.text = resources.getString(R.string.analytics_dashboard)
            f5.text = resources.getString(R.string.user_segmentation)

            loadIcon(R.drawable.ic_notification_service)
        }
    }

    fun setAdmobTypeData(){
        binding.apply {
            appTitle.text = resources.getString(R.string.admob_monetization)
            appMsg.text = resources.getString(R.string.integrate_ads_and_start_earning_from_your_app)

            f1.text = resources.getString(R.string.banner_ads_integration)
            f2.text = resources.getString(R.string.interstitial_ads)
            f3.text = resources.getString(R.string.reward_ads)
            f4.text = resources.getString(R.string.admob_account_setup)
            f5.text = resources.getString(R.string.revenue_optimization)

            loadIcon(R.drawable.ic_admob)
        }
    }

    fun setProBuildTypeData(){
        binding.apply {
            appTitle.text = resources.getString(R.string.premium_build_no_watermark)
            appMsg.text = resources.getString(R.string.remove_watermark_and_get_premium_features)

            f1.text = resources.getString(R.string.no_watermark)
            f2.text = resources.getString(R.string.priority_support)
            f3.text = resources.getString(R.string.custom_domain)
            f4.text = resources.getString(R.string.advanced_analytics)
            f5.text = resources.getString(R.string.unlimited_rebuilds)

            loadIcon(R.drawable.ic_pro_build)
        }
    }

    fun loadIcon(icon: Int = R.drawable.ic_publish_app){
        Glide.with(applicationContext)
            .load(icon)
            .into(binding.appIconIv)
    }
}