package com.app.styletap.webtoappconverter.presentations.ui.activities.onboarding

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityOnboardingBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.presentations.ui.activities.authorization.LoginActivity
import com.app.styletap.webtoappconverter.presentations.utils.Contants.isShowOnBoarding
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper

class OnboardingActivity : AppCompatActivity() {
    lateinit var binding: ActivityOnboardingBinding
    var introCounter = 1
    var fromWhere = "home"

    private lateinit var prefHelper: PrefHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        customEnableEdgeToEdge()

        adjustBottomHeight(binding.container)
        prefHelper = PrefHelper(this.applicationContext)

        initiView()
    }

    fun initiView() {

        binding.skipBtn.setOnClickListener{
            introCounter = 4
            onNext()
        }

        binding.nextBtn.setOnClickListener{
            introCounter++
            onNext()
        }


        binding.getStartedBtn.setOnClickListener{
            introCounter++
            onNext()
        }



    }

    fun onNext(){
        if (introCounter < 4){
            binding.apply {
                slide1.isVisible = introCounter == 1
                slide2.isVisible = introCounter == 2
                slide3.isVisible = introCounter == 3

                getStartedBtn.isVisible = introCounter >= 3
                skipBtn.isVisible = introCounter < 3
                nextBtn.isVisible = introCounter < 3

                if (introCounter == 1){
                    dot1.setImageResource(R.drawable.dot_selected_board)
                    dot2.setImageResource(R.drawable.dot_unselected_board)
                    dot3.setImageResource(R.drawable.dot_unselected_board)
                } else if (introCounter == 2){
                    dot1.setImageResource(R.drawable.dot_unselected_board)
                    dot2.setImageResource(R.drawable.dot_selected_board)
                    dot3.setImageResource(R.drawable.dot_unselected_board)
                } else {
                    dot1.setImageResource(R.drawable.dot_unselected_board)
                    dot2.setImageResource(R.drawable.dot_unselected_board)
                    dot3.setImageResource(R.drawable.dot_selected_board)
                }

            }
        } else {
            prefHelper.setBoolean(isShowOnBoarding, false)

            val mIntent = Intent(this, LoginActivity::class.java).apply {
                putExtra("from", "splash")
            }
            startActivity(mIntent)
            finish()
        }
    }


}