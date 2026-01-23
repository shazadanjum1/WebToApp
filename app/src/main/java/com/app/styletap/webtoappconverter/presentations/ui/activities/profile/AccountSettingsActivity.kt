package com.app.styletap.webtoappconverter.presentations.ui.activities.profile

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityAccountSettingsBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.enablePasswordToggle
import com.app.styletap.webtoappconverter.extentions.isNetworkAvailable
import com.app.styletap.webtoappconverter.models.User
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ACTION_REFRESH_ACTIVITY
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AccountSettingsActivity : AppCompatActivity() {
    lateinit var binding: ActivityAccountSettingsBinding

    var isClickable = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityAccountSettingsBinding.inflate(layoutInflater)
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

        initView()

    }

    fun onBack() {
        if (isClickable){
            finish()
        }
    }

    fun initView(){
        binding.apply {
            toolbar.titleTv.text = resources.getString(R.string.account_settings)
            toolbar.backBtn.isVisible = true
            toolbar.backBtn.setOnClickListener {
                onBack()
            }

            etCtPassword.enablePasswordToggle(
                startDrawable = R.drawable.ic_lock,
                eyeOnDrawable = R.drawable.ic_eye_on,
                eyeOffDrawable = R.drawable.ic_eye_off
            )

            etNwPassword.enablePasswordToggle(
                startDrawable = R.drawable.ic_lock,
                eyeOnDrawable = R.drawable.ic_eye_on,
                eyeOffDrawable = R.drawable.ic_eye_off
            )

            etConfirmPassword.enablePasswordToggle(
                startDrawable = R.drawable.ic_lock,
                eyeOnDrawable = R.drawable.ic_eye_on,
                eyeOffDrawable = R.drawable.ic_eye_off
            )

            btnSave.setOnClickListener {
                val name = binding.etName.text.toString().trim()

                if (name.isEmpty()) {
                    binding.etName.error = resources.getString(R.string.name_is_required)
                    binding.etName.requestFocus()
                    return@setOnClickListener
                }

                if (!isNetworkAvailable()){
                    Toast.makeText(this@AccountSettingsActivity, resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (isClickable){
                    updatePersonalInfo(name)
                }

            }

            btnUpdatePassword.setOnClickListener {
                btnUpdatePassword()
            }

            fetchMyApps()
        }
    }

    fun fetchMyApps() {
        if (!isNetworkAvailable()){
            Toast.makeText(this@AccountSettingsActivity, resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        isClickable = false
        binding.progressBar.isVisible = true

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
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

                val user = document.toObject(User::class.java)
                if (user != null) {
                    binding.apply {
                        etName.setText(user.name)
                        etEmail.setText(user.email)
                    }
                } else {
                    Toast.makeText(this@AccountSettingsActivity, resources.getString(R.string.something_went_wrong_try_again), Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }
            }
            .addOnFailureListener {
                isClickable = true
                binding.progressBar.isVisible = false
                Toast.makeText(this@AccountSettingsActivity, resources.getString(R.string.something_went_wrong_try_again), Toast.LENGTH_SHORT).show()
                finish()
            }

    }



    fun updatePersonalInfo(fullName: String){

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        isClickable = false
        binding.progressBar.isVisible = true

        val userDetails = hashMapOf<String, Any?>(
            "name" to fullName,
        )

        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .update(userDetails)
            .addOnSuccessListener {
                isClickable = true
                binding.progressBar.isVisible = false
                Toast.makeText(this, resources.getString(R.string.updated_successfully), Toast.LENGTH_SHORT).show()
                sendBroadcast(Intent(ACTION_REFRESH_ACTIVITY).apply { setPackage(packageName) })
                /*
                startActivity(Intent(this@BuildingAppActivity, MyAppsActivity::class.java))
                finish()*/
            }
            .addOnFailureListener { e ->
                isClickable = true
                binding.progressBar.isVisible = false
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }


    fun btnUpdatePassword() {
        binding.apply {
            val currentPassword = etCtPassword.text.toString().trim()
            val newPassword = etNwPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // 1️⃣ Validation
            when {
                currentPassword.isEmpty() -> {
                    etCtPassword.error = resources.getString(R.string.enter_current_password)
                    etCtPassword.requestFocus()
                    return
                }
                newPassword.isEmpty() -> {
                    etNwPassword.error = resources.getString(R.string.enter_new_password)
                    etNwPassword.requestFocus()
                    return
                }
                newPassword.length < 6 -> {
                    etNwPassword.error = resources.getString(R.string.password_should_be_at_least_6_characters)
                    etNwPassword.requestFocus()
                    return
                }
                confirmPassword.isEmpty() -> {
                    etConfirmPassword.error = resources.getString(R.string.confirm_new_password)
                    etConfirmPassword.requestFocus()
                    return
                }
                newPassword != confirmPassword -> {
                    etConfirmPassword.error = resources.getString(R.string.passwords_do_not_match)
                    etConfirmPassword.requestFocus()
                    return
                }
            }

            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                return
            }

            if (!isNetworkAvailable()){
                Toast.makeText(this@AccountSettingsActivity, resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                return
            }

            isClickable = false
            binding.progressBar.isVisible = true

            // 2️⃣ Re-authenticate
            val email = user.email ?: return
            val credential = EmailAuthProvider.getCredential(email, currentPassword)

            user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    // 3️⃣ Update password
                    user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                        isClickable = true
                        binding.progressBar.isVisible = false
                        if (updateTask.isSuccessful) {
                            etCtPassword.setText("")
                            etNwPassword.setText("")
                            etConfirmPassword.setText("")
                            Toast.makeText(this@AccountSettingsActivity, resources.getString(R.string.password_updated_successfully), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@AccountSettingsActivity, "Failed to update password: ${updateTask.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    } .addOnFailureListener {
                        isClickable = true
                        binding.progressBar.isVisible = false
                        Toast.makeText(this@AccountSettingsActivity, resources.getString(R.string.something_went_wrong_try_again), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    isClickable = true
                    binding.progressBar.isVisible = false
                    Toast.makeText(this@AccountSettingsActivity, resources.getString(R.string.current_password_is_incorrect), Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                    isClickable = true
                    binding.progressBar.isVisible = false
                Toast.makeText(this@AccountSettingsActivity, resources.getString(R.string.something_went_wrong_try_again), Toast.LENGTH_SHORT).show()
            }
        }

    }

}