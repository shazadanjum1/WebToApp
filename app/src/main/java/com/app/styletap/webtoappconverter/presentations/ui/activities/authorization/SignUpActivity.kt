package com.app.styletap.webtoappconverter.presentations.ui.activities.authorization

import android.annotation.SuppressLint
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
import com.app.styletap.webtoappconverter.databinding.ActivityLoginBinding
import com.app.styletap.webtoappconverter.databinding.ActivitySignUpBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.enablePasswordToggle
import com.app.styletap.webtoappconverter.extentions.isNetworkAvailable
import com.app.styletap.webtoappconverter.extentions.setClickableText
import com.app.styletap.webtoappconverter.extentions.setMultiClickableText
import com.app.styletap.webtoappconverter.presentations.ui.activities.home.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.system.exitProcess

class SignUpActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding
    var isClickable = true

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivitySignUpBinding.inflate(layoutInflater)
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
        db = FirebaseFirestore.getInstance()

        initView()
    }

    fun onBack(){
        if (isClickable){
            finish()
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    fun initView(){
        binding.apply {
            toolbar.titleTv.text = resources.getString(R.string.create_account)
            etPassword.enablePasswordToggle(
                startDrawable = R.drawable.ic_lock,
                eyeOnDrawable = R.drawable.ic_eye_on,
                eyeOffDrawable = R.drawable.ic_eye_off
            )
            etConfirmPassword.enablePasswordToggle(
                startDrawable = R.drawable.ic_lock,
                eyeOnDrawable = R.drawable.ic_eye_on,
                eyeOffDrawable = R.drawable.ic_eye_off
            )


            val signUpText = getString(R.string.sign_in_s)
            val fullText = getString(R.string.already_have_an_account, signUpText)

            tvSignIn.setClickableText(
                fullText = fullText,
                clickableText = signUpText,
                clickableColor = R.color.blue,
                underline = false
            ) {
                onBack()
            }


            val privacyText = getString(R.string.privacy_policy)
            val termsText = getString(R.string.terms_of_service)

            val fullTextPT = getString(
                R.string.i_agree_to_the_terms_of_service_and_privacy_policy,
                termsText,
                privacyText
            )

            forgetPasswordBtn.setMultiClickableText(
                fullText = fullTextPT,
                clickableColor = R.color.blue,
                underline = false,
                clickableParts = mapOf(
                    termsText to {
                        // Open Terms of Service
                        if (isClickable){

                        }
                    },
                    privacyText to {
                        // Open Privacy Policy
                        if (isClickable){

                        }
                    }
                )
            )


            binding.btnRegister.setOnClickListener {
                val name = binding.etName.text.toString().trim()
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()

                if (!validateInputs(name, email, password)) return@setOnClickListener
                if (!isNetworkAvailable()){
                    Toast.makeText(this@SignUpActivity, resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (isClickable){
                    isClickable = false
                    binding.progressBar.isVisible = true
                    registerUser(name, email, password)
                }


            }

        }

    }

    private fun validateInputs(name: String, email: String, password: String): Boolean {
        if (name.isEmpty()) {
            binding.etName.error = resources.getString(R.string.name_is_required)
            binding.etName.requestFocus()
            return false
        }

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

        if (password.isEmpty()) {
            binding.etPassword.error = resources.getString(R.string.password_is_required)
            binding.etPassword.requestFocus()
            return false
        }

        if (password.length < 6) {
            binding.etPassword.error = resources.getString(R.string.password_must_be_at_least_6_characters)
            binding.etPassword.requestFocus()
            return false
        }

        return true
    }

    private fun registerUser(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val timestamp = System.currentTimeMillis()

                    FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                        val fcmToken = if (tokenTask.isSuccessful) tokenTask.result else ""

                        val userMap = hashMapOf(
                            "uid" to userId,
                            "name" to name,
                            "email" to email,
                            "phone" to "",
                            "fcmToken" to fcmToken,
                            "signupDate" to timestamp,
                            "lastLoginDate" to timestamp
                        )

                        if (userId != null) {
                            db.collection("users").document(userId)
                                .set(userMap, SetOptions.merge())
                                .addOnSuccessListener {
                                    Toast.makeText(this, resources.getString(R.string.registered_successfully), Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finishAffinity()
                                }
                                .addOnFailureListener { e ->
                                    isClickable = true
                                    binding.progressBar.isVisible = false
                                    Toast.makeText(this, "error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                } else {
                    isClickable = true
                    binding.progressBar.isVisible = false
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }



}