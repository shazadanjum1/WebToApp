package com.app.styletap.webtoappconverter.presentations.ui.activities.authorization

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityForgetBinding
import com.app.styletap.webtoappconverter.databinding.ActivityLoginBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.isNetworkAvailable
import com.app.styletap.webtoappconverter.extentions.setClickableText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ForgetActivity : AppCompatActivity() {
    lateinit var binding: ActivityForgetBinding
    var isClickable = true

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeLocale()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityForgetBinding.inflate(layoutInflater)
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

        initView()
    }

    fun onBack(){
        if (isClickable){
            finish()
        }
    }

    fun initView(){
        binding.apply {
            toolbar.titleTv.text = resources.getString(R.string.forgot_password_)

            binding.btnLogin.setOnClickListener {
                val email = binding.etEmail.text.toString().trim()

                if (!validateInputs(email)) return@setOnClickListener

                if (!isNetworkAvailable()){
                    Toast.makeText(this@ForgetActivity, resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }


                if (isClickable){
                    isClickable = false
                    binding.progressBar.isVisible = true
                    sendPasswordResetEmail(email)
                }

            }
        }
    }

    private fun validateInputs(email: String): Boolean {
        if (email.isEmpty()) {
            binding.etEmail.error = resources.getString(R.string.email_is_required)
            binding.etEmail.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = resources.getString(R.string.please_enter_a_valid_email)
            binding.etEmail.requestFocus()
            return false
        }

        return true
    }

    private fun sendPasswordResetEmail(email: String) {
        binding.progressBar.isVisible = true
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                isClickable = true
                binding.progressBar.isVisible = false
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.reset_password_email_sent), Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }


}