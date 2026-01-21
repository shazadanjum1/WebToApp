package com.app.styletap.webtoappconverter.presentations.ui.activities.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.presentations.ui.activities.authorization.LoginActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.home.MainActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.onboarding.OnboardingActivity
import com.app.styletap.webtoappconverter.presentations.utils.Contants.isShowOnBoarding
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private lateinit var prefHelper: PrefHelper

    private lateinit var auth: FirebaseAuth
    var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        customEnableEdgeToEdge()

        prefHelper = PrefHelper(this.applicationContext)
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            moveNext()
        }

    }


    fun moveNext(){
        /*val mIntent = if (user == null){
            if (prefHelper.getBooleanDefultTrue(isShowOnBoarding)){
                Intent(this, OnboardingActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
        } else {
            Intent(this, MainActivity::class.java)
        }*/

        val mIntent =
            if (user?.isAnonymous == true) {
                Intent(this, MainActivity::class.java)
            } else if (user == null) {
                if (prefHelper.getBooleanDefultTrue(isShowOnBoarding)){
                    Intent(this, OnboardingActivity::class.java)
                } else {
                    Intent(this, LoginActivity::class.java)
                }
            } else {
                Intent(this, MainActivity::class.java)
            }


        mIntent.apply {
            putExtra("from", "splash")
        }

        startActivity(mIntent)
        finish()
    }
}