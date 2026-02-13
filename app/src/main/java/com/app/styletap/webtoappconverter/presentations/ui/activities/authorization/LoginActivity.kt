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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.app.styletap.interfaces.FirebaseAnalyticsUtils
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityLoginBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.enablePasswordToggle
import com.app.styletap.webtoappconverter.extentions.isNetworkAvailable
import com.app.styletap.webtoappconverter.extentions.isValidEmail
import com.app.styletap.webtoappconverter.extentions.setClickableText
import com.app.styletap.webtoappconverter.presentations.ui.activities.home.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    var isClickable = true

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    var fromWhere = "splash"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeLocale()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        customEnableEdgeToEdge()

        adjustTopHeight(binding.toolbarLL)
        //adjustBottomHeight(binding.container)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())

            val bottomInset = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            ).bottom

            v.updatePadding(bottom = bottomInset)

            insets
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBack()
                }
            })

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        intent?.extras?.let {
            fromWhere = it.getString("from", "splash")
        }

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
            toolbar.titleTv.text = resources.getString(R.string.login)

            if (fromWhere == "settings"){
                toolbar.backBtn.isVisible = true
                toolbar.backBtn.setOnClickListener {
                    onBack()
                }
            }

            etPassword.enablePasswordToggle(
                startDrawable = R.drawable.ic_lock,
                eyeOnDrawable = R.drawable.ic_eye_on,
                eyeOffDrawable = R.drawable.ic_eye_off
            )

            val signUpText = getString(R.string.sign_up_for_free)
            val fullText = getString(R.string.dont_have_an_account, signUpText)

            tvSignup.setClickableText(
                fullText = fullText,
                clickableText = signUpText,
                clickableColor = R.color.blue,
                underline = false
            ) {
                if (isClickable){
                    val mIntent = Intent(this@LoginActivity, SignUpActivity::class.java).apply {
                        putExtra("from", "login")
                    }
                    startActivity(mIntent)
                    //finish()
                }

            }

            forgetPasswordBtn.setOnClickListener {
                startActivity(Intent(this@LoginActivity, ForgetActivity::class.java))
            }

            btnLogin.setOnClickListener {
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()

                if (!validateInputs(email, password)) return@setOnClickListener

                if (!isNetworkAvailable()){
                    Toast.makeText(this@LoginActivity, resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (isClickable){
                    isClickable = false
                    progressBar.isVisible = true
                    loginUser(email, password)
                }

            }

            btnGuestLogin.setOnClickListener {

                if (!isNetworkAvailable()){
                    Toast.makeText(this@LoginActivity, resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (isClickable){
                    isClickable = false
                    progressBar.isVisible = true
                    loginAsGuest()
                }

            }
        }


    }

    private fun validateInputs(email: String, password: String): Boolean {
       if (email.isEmpty()) {
            binding.etEmail.error = resources.getString(R.string.email_is_required)
            binding.etEmail.requestFocus()
            return false
        }

        /*if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = resources.getString(R.string.please_enter_a_valid_email)
            binding.etEmail.requestFocus()
            return false
        }*/

        if (!isValidEmail(email)) {
            binding.etEmail.error = getString(R.string.please_enter_a_valid_email)
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

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val loginTimestamp = System.currentTimeMillis()

                    FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                        val fcmToken = if (tokenTask.isSuccessful) tokenTask.result else ""

                        val updateMap = hashMapOf<String, Any>(
                            "lastLoginDate" to loginTimestamp,
                            "fcmToken" to fcmToken
                        )

                        if (userId != null) {
                            db.collection("users").document(userId)
                                .set(updateMap, SetOptions.merge()) // merge-safe
                                .addOnSuccessListener {
                                    Toast.makeText(this, resources.getString(R.string.login_successful), Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finishAffinity()
                                }
                                .addOnFailureListener { e ->
                                    FirebaseAnalyticsUtils.logEventMessage("server_error")
                                    isClickable = true
                                    binding.progressBar.isVisible = false
                                    Toast.makeText(this,  "error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                } else {
                    FirebaseAnalyticsUtils.logEventMessage("server_error")
                    isClickable = true
                    binding.progressBar.isVisible = false
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


    fun loginAsGuest(){
        FirebaseAuth.getInstance()
            .signInAnonymously()
            .addOnSuccessListener { result ->

                val user = result.user
                val uid = user?.uid

                val userId = uid //auth.currentUser?.uid
                val timestamp = System.currentTimeMillis()

                FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                    val fcmToken = if (tokenTask.isSuccessful) tokenTask.result else ""

                    val userMap = hashMapOf(
                        "uid" to userId,
                        "name" to "Guest",
                        "email" to "",
                        "phone" to "",
                        "fcmToken" to fcmToken,
                        "signupDate" to timestamp,
                        "lastLoginDate" to timestamp
                    )

                    if (userId != null) {
                        db.collection("users").document(userId)
                            .set(userMap, SetOptions.merge())
                            .addOnSuccessListener {
                                isClickable = true
                                binding.progressBar.isVisible = false
                                Toast.makeText(this, resources.getString(R.string.login_successful), Toast.LENGTH_SHORT).show()
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
            }
            .addOnFailureListener { e ->
                FirebaseAnalyticsUtils.logEventMessage("server_error")
                isClickable = true
                binding.progressBar.isVisible = false
                Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()

            }

    }
}