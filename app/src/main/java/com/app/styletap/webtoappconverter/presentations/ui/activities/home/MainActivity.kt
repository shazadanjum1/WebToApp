package com.app.styletap.webtoappconverter.presentations.ui.activities.home

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityMainBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.isNetworkAvailable
import com.app.styletap.webtoappconverter.extentions.logoutUser
import com.app.styletap.webtoappconverter.presentations.ui.activities.createApp.CreateAppActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.myApps.MyAppsActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.serviceRequest.ServiceRequestActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.services.ServicesActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.support.SupportActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth
    var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        initView()
    }

    fun onBack(){
        finishAffinity()
        exitProcess(0)
    }

    fun initView(){
        binding.apply {
            toolbar.titleTv.text = resources.getString(R.string.dashboard)
            toolbar.signOutBtn.isVisible = true

            if (user?.isAnonymous == true){
                toolbar.profileBtn.isVisible = false
            } else {
                toolbar.profileBtn.isVisible = true
            }

            toolbar.signOutBtn.setOnClickListener {
                logoutUser()
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

        }
    }

    fun moveNext(intent: Intent){
        startActivity(intent)
    }
}