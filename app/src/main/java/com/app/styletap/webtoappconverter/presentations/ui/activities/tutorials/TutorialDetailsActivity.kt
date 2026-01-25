package com.app.styletap.webtoappconverter.presentations.ui.activities.tutorials

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityTutorialDetailsBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.presentations.utils.Contants.CREATE_APP
import com.app.styletap.webtoappconverter.presentations.utils.Contants.CREATE_BUNDLE
import com.app.styletap.webtoappconverter.presentations.utils.Contants.UPLOAD_APP

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


            if (serviceType == CREATE_BUNDLE){
                setCreateBundleData()
            } else if (serviceType == UPLOAD_APP){
                setUploadAppData()
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
        }
    }

    fun setCreateBundleData(){
        binding.apply {
            appTitle.text = resources.getString(R.string.how_to_create_an_app_bundle)
            appMsg.text = resources.getString(R.string.step_by_step_guide_to_create_your_app_bundle)

            f1.text = resources.getString(R.string.open_my_apps)
            f2.text = resources.getString(R.string.start_bundle_generation)
            f3.text = resources.getString(R.string.bundle_generation_in_progress)
            f4.text = resources.getString(R.string.bundle_ready_for_download)
            f5.text = resources.getString(R.string.download_generated_bundle)
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

        }
    }

}