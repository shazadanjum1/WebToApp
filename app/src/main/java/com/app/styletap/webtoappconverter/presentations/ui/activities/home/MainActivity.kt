package com.app.styletap.webtoappconverter.presentations.ui.activities.home

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.app.styletap.ads.BannerAdManager
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityMainBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.isNetworkAvailable
import com.app.styletap.webtoappconverter.extentions.logoutUser
import com.app.styletap.webtoappconverter.extentions.openEmail
import com.app.styletap.webtoappconverter.extentions.showLogoutDialog
import com.app.styletap.webtoappconverter.extentions.withNotificationPermission
import com.app.styletap.webtoappconverter.presentations.ui.activities.createApp.CreateAppActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.myApps.MyAppsActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.profile.ProfileActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.serviceRequest.ServiceRequestActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.services.ServicesActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.support.SupportActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.tutorials.TutorialsActivity
import com.app.styletap.webtoappconverter.presentations.utils.Contants.home_banner
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth
    var user: FirebaseUser? = null
    private lateinit var prefHelper: PrefHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeLocale()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityMainBinding.inflate(layoutInflater)
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

        prefHelper = PrefHelper(this.applicationContext)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        initView()

        withNotificationPermission{}

        showBannerAd()
    }

    fun onBack(){
        finishAffinity()
        exitProcess(0)
    }

    fun initView(){
        binding.apply {
            toolbar.titleTv.text = resources.getString(R.string.dashboard)
            //toolbar.signOutBtn.isVisible = true
            toolbar.profileBtn.isVisible = true

            if (user?.isAnonymous == true){
                toolbar.signOutBtn.isVisible = false
                //toolbar.profileBtn.isVisible = false
            } else {
                //toolbar.profileBtn.isVisible = true
                toolbar.signOutBtn.isVisible = true

            }

            toolbar.signOutBtn.setOnClickListener {
                showLogoutDialog{
                    logoutUser()
                }
            }


            toolbar.profileBtn.setOnClickListener {
                if (isNetworkAvailable()){
                    moveNext(Intent(this@MainActivity, ProfileActivity::class.java))
                } else {
                    Toast.makeText(this@MainActivity, resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                }
            }


            createAppBtn.setOnClickListener {
                moveNext(Intent(this@MainActivity, CreateAppActivity::class.java))
            }

            myAppsBtn.setOnClickListener {
                if (isNetworkAvailable()){
                    moveNext(Intent(this@MainActivity, MyAppsActivity::class.java))
                } else {
                    Toast.makeText(this@MainActivity, resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                }
            }

            waSupportBtn.setOnClickListener {
                moveNext(Intent(this@MainActivity, SupportActivity::class.java))
            }

            promoBtn.setOnClickListener {
                moveNext(Intent(this@MainActivity, ServiceRequestActivity::class.java))
            }

            servicesBtn.setOnClickListener {
                moveNext(Intent(this@MainActivity, ServicesActivity::class.java))
            }

            tutorialsBtn.setOnClickListener {
                moveNext(Intent(this@MainActivity, TutorialsActivity::class.java))
            }

            feedbackBtn.setOnClickListener {
                openEmail(
                    email = getString(R.string.email),
                    subject = "Feedback Web To App Converter",
                    message = ""
                )
            }

        }
    }

    fun moveNext(intent: Intent){
        startActivity(intent)
    }

    fun showBannerAd(){
        if (isNetworkAvailable() && prefHelper.getBooleanDefultTrue(home_banner) && !prefHelper.getIsPurchased()){
            binding.adLayout.visibility = View.VISIBLE
            binding.bannerShimmerView.root.visibility = View.VISIBLE
            binding.bannerShimmerView.bannerShimmerView.startShimmer()
            BannerAdManager(this).loadAndShowBannerAd(resources.getString(R.string.homeBannerId) , binding.adFrame, binding.adLayout, binding.bannerShimmerView.bannerShimmerView)
        } else {
            binding.adLayout.visibility = View.GONE
            binding.bannerShimmerView.bannerShimmerView.stopShimmer()
        }
    }
}