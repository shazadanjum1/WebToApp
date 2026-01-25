package com.app.styletap.webtoappconverter.presentations.ui.activities.tutorials

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityTutorialDetailsBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.presentations.utils.Contants.CREATE_APP
import com.app.styletap.webtoappconverter.presentations.utils.Contants.CUSTOM_DOMAIN
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ENABLE_MONITIZATION
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ENABLE_NOTIFICATIONS
import com.app.styletap.webtoappconverter.presentations.utils.Contants.UPLOAD_APP
import com.bumptech.glide.Glide

class TutorialDetailsActivity : AppCompatActivity() {
    lateinit var binding: ActivityTutorialDetailsBinding

    var serviceType: String = CREATE_APP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeLocale()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityTutorialDetailsBinding.inflate(layoutInflater)
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
            serviceType = it.getString("serviceType", CREATE_APP)
        }

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


            if (serviceType == UPLOAD_APP){
                setUploadAppData()
            } else if (serviceType == ENABLE_NOTIFICATIONS){
                setEnableNotificationData()
            } else if (serviceType == CUSTOM_DOMAIN){
                setCustomDomainData()
            } else if (serviceType == ENABLE_MONITIZATION){
                setAdmobData()
            } else {
                setCreateAppData()
            }

        }
    }


    fun setCreateAppData(){
        binding.apply {
            appTitle.text = resources.getString(R.string.how_to_create_an_app)
            appMsg.text = resources.getString(R.string.step_by_step_guide_to_create_your_first_web_to_app_conversion)

            f1.text = resources.getString(R.string.enter_your_website_url)
            f2.text = resources.getString(R.string.fill_in_app_basic_information)
            f3.text = resources.getString(R.string.customize_branding_and_colors)
            f4.text = resources.getString(R.string.configure_app_features)
            f5.text = resources.getString(R.string.generate_and_download_apk)

            iv1.loadImage(R.drawable.ic_create_app_btn)
            tagTv.text = resources.getString(R.string.getting_started)
        }
    }

    fun setUploadAppData(){
        binding.apply {
            appTitle.text = resources.getString(R.string.how_to_upload_to_play_store)
            appMsg.text = resources.getString(R.string.complete_guide_to_publish_your_app_on_google_play_store)

            f1.text = resources.getString(R.string.create_a_google_play_developer_account)
            f2.text = resources.getString(R.string.prepare_app_assets_and_screenshots)
            f3.text = resources.getString(R.string.fill_in_store_listing_details)
            f4.text = resources.getString(R.string.set_up_pricing_and_distribution)
            f5.text = resources.getString(R.string.submit_for_review)

            iv1.loadImage(R.drawable.ic_upload_app_btn)
            tagTv.text = resources.getString(R.string.publishing)

        }
    }

    fun setEnableNotificationData(){
        binding.apply {
            appTitle.text = resources.getString(R.string.how_to_enable_notifications)
            appMsg.text = resources.getString(R.string.setup_push_notifications_for_your_android_app)

            f1.text = resources.getString(R.string.create_firebase_project)
            f2.text = resources.getString(R.string.add_firebase_to_your_app)
            f3.text = resources.getString(R.string.configure_notification_settings)
            f4.text = resources.getString(R.string.test_notifications)
            f5.text = resources.getString(R.string.schedule_automated_notifications)

            iv1.loadImage(R.drawable.ic_enable_noti_btn)
            tagTv.text = resources.getString(R.string.features)

        }
    }

    fun setCustomDomainData(){
        binding.apply {
            appTitle.text = resources.getString(R.string.how_to_add_custom_domain)
            appMsg.text = resources.getString(R.string.connect_your_own_domain_to_your_app)

            f1.text = resources.getString(R.string.purchase_your_domain)
            f2.text = resources.getString(R.string.access_domain_dns_settings)
            f3.text = resources.getString(R.string.add_required_dns_records)
            f4.text = resources.getString(R.string.verify_domain_ownership)
            f5.text = resources.getString(R.string.update_app_configuration)

            iv1.loadImage(R.drawable.ic_custom_comain_btn)
            tagTv.text = resources.getString(R.string.advanced)

        }
    }

    fun setAdmobData(){
        binding.apply {
            appTitle.text = resources.getString(R.string.how_to_use_admob)
            appMsg.text = resources.getString(R.string.monetize_your_app_with_google_admob)

            f1.text = resources.getString(R.string.create_admob_account)
            f2.text = resources.getString(R.string.create_ad_units)
            f3.text = resources.getString(R.string.integrate_admob_sdk)
            f4.text = resources.getString(R.string.implement_different_ad_formats)
            f5.text = resources.getString(R.string.track_revenue_and_performance)

            iv1.loadImage(R.drawable.ic_admob_btn)
            tagTv.text = resources.getString(R.string.monetization)

        }
    }


    fun AppCompatImageView.loadImage(drawableRes: Int){
        Glide.with(this)
            .load(drawableRes)
            .centerCrop()
            .into(this)
    }

}