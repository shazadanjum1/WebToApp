package com.app.styletap.webtoappconverter.presentations.ui.activities.support


import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.app.styletap.interfaces.FirebaseAnalyticsUtils
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivitySupportBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.openEmail
import com.app.styletap.webtoappconverter.extentions.openWhatsApp

class SupportActivity : AppCompatActivity() {
    lateinit var binding: ActivitySupportBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeLocale()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivitySupportBinding.inflate(layoutInflater)
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

        initView()

    }

    fun onBack() {
        finish()
    }

    fun initView(){
        binding.apply {
            toolbar.titleTv.text = resources.getString(R.string.support)
            toolbar.backBtn.isVisible = true
            toolbar.backBtn.setOnClickListener {
                onBack()
            }

            waCard.setOnClickListener {
                FirebaseAnalyticsUtils.logEventMessage("agency_whatsapp_click")
                openWhatsApp(getString(R.string.phone_number))
            }

            submitBtn.setOnClickListener {
                FirebaseAnalyticsUtils.logEventMessage("agency_contact_click")
                openEmail(
                    email = getString(R.string.email),
                    subject = etIssueType.text.toString(),
                    message = etMessage.text.toString()
                )
            }

        }
    }
}