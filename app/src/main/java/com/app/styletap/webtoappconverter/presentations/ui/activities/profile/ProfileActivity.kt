package com.app.styletap.webtoappconverter.presentations.ui.activities.profile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityProfileBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.formatDate
import com.app.styletap.webtoappconverter.extentions.getInitials
import com.app.styletap.webtoappconverter.extentions.getTimeInMillis
import com.app.styletap.webtoappconverter.extentions.logoutUser
import com.app.styletap.webtoappconverter.extentions.openLink
import com.app.styletap.webtoappconverter.extentions.showLogoutDialog
import com.app.styletap.webtoappconverter.models.User
import com.app.styletap.webtoappconverter.presentations.ui.activities.authorization.LoginActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.home.MainActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.language.LanguageActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.services.ServiceDetailsActivity
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ACTION_REFRESH_ACTIVITY
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class ProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityProfileBinding

    var isClickable = true

    private lateinit var auth: FirebaseAuth
    var user: FirebaseUser? = null

    private val refreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_REFRESH_ACTIVITY) {
                fetchMyDetails()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeLocale()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        customEnableEdgeToEdge()

        adjustTopHeight(binding.toolbarLL)
        adjustBottomHeight(binding.container)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBack()
                }
            })

        initView()

        registerReceiver(
            refreshReceiver,
            IntentFilter(ACTION_REFRESH_ACTIVITY),
            Context.RECEIVER_NOT_EXPORTED // required for Android 13+
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(refreshReceiver)
        }catch (_: Exception){}
    }

    fun onBack() {
        if (isClickable){
            finish()
        }
    }

    fun initView(){
        binding.apply {
            toolbar.titleTv.text = resources.getString(R.string.profile)
            toolbar.backBtn.isVisible = true
            toolbar.backBtn.setOnClickListener {
                onBack()
            }

            if (user?.isAnonymous == true) {
                guestCard.isVisible = true
                accountInfoCard.isVisible = false
                accountSettingsBtn.isVisible = false
                editIv.isVisible = false
                initCard.isClickable = false
                btnLogout.isVisible = false
            } else {
                guestCard.isVisible = false
                accountInfoCard.isVisible = true
                accountSettingsBtn.isVisible = true
                editIv.isVisible = true
                initCard.isClickable = true
                btnLogout.isVisible = true
            }

            fetchMyDetails()

            try {
                val versionName = packageManager.getPackageInfo(packageName, 0).versionName
                versionTv.text = "${resources.getString(R.string.version)}: $versionName"

            } catch (_: Exception){}


            accountSettingsBtn.setOnClickListener {
                startActivity(Intent(this@ProfileActivity, AccountSettingsActivity::class.java))
            }

            initCard.setOnClickListener {
                startActivity(Intent(this@ProfileActivity, AccountSettingsActivity::class.java))
            }

            languageBtn.setOnClickListener {
                startActivity(
                    Intent(this@ProfileActivity, LanguageActivity::class.java).apply {
                        putExtra("from", "profile")
                    }
                )
            }

            privacyBtn.setOnClickListener {
                openLink(resources.getString(R.string.privacy_policy_link))
            }

            btnLogout.setOnClickListener {
                showLogoutDialog{
                    logoutUser()
                }
            }

            guestCard.setOnClickListener {
                val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                //finish()
            }
        }
    }

    fun fetchMyDetails() {
        isClickable = false
        binding.progressBar.isVisible = true

        val userId = user?.uid ?: run {
            finish()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                isClickable = true
                binding.progressBar.isVisible = false

                val userModel = document.toObject(User::class.java)
                if (userModel != null) {
                    binding.apply {
                        if (user?.isAnonymous == true) {
                            avatarText.text = "GU"
                            fullNameTv.text = "Guest User"
                        } else {
                            avatarText.text = getInitials(userModel.name)
                            fullNameTv.text = userModel.name
                            emailTv.text = userModel.email
                            dateTv.text = formatDate(getTimeInMillis(userModel.signupDate))

                        }

                    }
                } else {
                    Toast.makeText(this@ProfileActivity, resources.getString(R.string.something_went_wrong_try_again), Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }
            }
            .addOnFailureListener {
                isClickable = true
                binding.progressBar.isVisible = false
                Toast.makeText(this@ProfileActivity, resources.getString(R.string.something_went_wrong_try_again), Toast.LENGTH_SHORT).show()
                finish()
            }

    }

        fun moveNext(serviceType: String){
        val mIntent = Intent(this@ProfileActivity, ServiceDetailsActivity::class.java).apply {
            putExtra("serviceType", serviceType)
        }
        startActivity(mIntent)
    }
}