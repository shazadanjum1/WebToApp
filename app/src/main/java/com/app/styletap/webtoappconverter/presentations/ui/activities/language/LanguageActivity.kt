package com.app.styletap.webtoappconverter.presentations.ui.activities.language

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityLanguageBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.adjustTopHeight
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.getLanguageData
import com.app.styletap.webtoappconverter.models.LanguageModel
import com.app.styletap.webtoappconverter.presentations.ui.activities.authorization.LoginActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.home.MainActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.language.adapters.LanguageAdapter
import com.app.styletap.webtoappconverter.presentations.ui.activities.onboarding.OnboardingActivity
import com.app.styletap.webtoappconverter.presentations.utils.Contants.isLanguageSelected
import com.app.styletap.webtoappconverter.presentations.utils.Contants.isShowOnBoarding
import com.app.styletap.webtoappconverter.presentations.utils.Contants.languageCode
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.Locale

class LanguageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLanguageBinding
    private lateinit var languageAdapter : LanguageAdapter

    var selectedLanguage = "en"
    lateinit var prefHelper: PrefHelper

    lateinit var lanuageModel: LanguageModel
    var fromWhere = "home"

    private lateinit var auth: FirebaseAuth
    var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeLocale()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        customEnableEdgeToEdge()

        adjustTopHeight(binding.toolbarLL)
        adjustBottomHeight(binding.container)


        prefHelper = PrefHelper(this)
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        lanuageModel = LanguageModel(R.drawable.flag_eng,"English","English","en",true)


        setView()
    }

    fun setView(){
        intent?.extras?.let {
            fromWhere = it.getString("from", "home")
        }

        binding.toolbar.titleTv.text = resources.getString(R.string.select_languages)

        selectedLanguage = prefHelper.getString(languageCode, "en").toString()

        val languageList = getLanguageData()

        findSelectedLanguage(selectedLanguage, languageList)

        languageAdapter = LanguageAdapter( languageList,this@LanguageActivity, selectedLanguage){
            if (lanuageModel.languageCode == it.languageCode){
                Toast.makeText(this@LanguageActivity, resources.getString(R.string.already_selected), Toast.LENGTH_SHORT).show()
            }
            lanuageModel = it
        }
        binding.languageRecyclerView .apply {
            layoutManager =  GridLayoutManager(this@LanguageActivity, 1)
            adapter = languageAdapter
        }

        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                languageAdapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackBtnPressed()
                }
            })

        binding.btnContinue.setOnClickListener{
            if (::lanuageModel.isInitialized){
                setLocale(lanuageModel.languageCode)
            }
        }
    }

    private fun onBackBtnPressed(){
        onBack()
    }

    fun findSelectedLanguage(languageCode: String, languageList: ArrayList<LanguageModel>){
        for (lanaguage in languageList){
            if (lanaguage.languageCode == languageCode){
                lanuageModel = lanaguage
                break
            }
        }
    }

    fun setLocale(language: String) {
        prefHelper.setBoolean(isLanguageSelected, true)
        prefHelper.setString(languageCode,language)

        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = Configuration(resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            configuration.setLocales(localeList)
        } else {
            configuration.setLocale(locale)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            createConfigurationContext(configuration)
        } else {
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        lonchHomeActivity()
    }

    private fun lonchHomeActivity(){

        /*if (fromWhere == "profile"){
            finish()
            return
        }
*/
        if (fromWhere == "splash" && (prefHelper.getBooleanDefultTrue(isShowOnBoarding))){
           // val mIntent = Intent(this@LanguageActivity, OnboardingActivity::class.java)


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

        } else {
            val mIntent = Intent(this@LanguageActivity, MainActivity::class.java)
            startActivity(mIntent)
            finishAffinity()
        }
    }

    private fun onBack(){

        if (fromWhere == "profile"){
            finish()
            return
        }

        if (fromWhere == "splash" && (prefHelper.getBooleanDefultTrue(isShowOnBoarding))){
            // val mIntent = Intent(this@LanguageActivity, OnboardingActivity::class.java)


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

        } else {
            val mIntent = Intent(this@LanguageActivity, MainActivity::class.java)
            startActivity(mIntent)
            finishAffinity()
        }
    }


}