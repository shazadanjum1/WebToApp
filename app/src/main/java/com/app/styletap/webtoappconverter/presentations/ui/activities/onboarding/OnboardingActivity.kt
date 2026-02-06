package com.app.styletap.webtoappconverter.presentations.ui.activities.onboarding

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.app.styletap.ads.NativeAdManager
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.ActivityOnboardingBinding
import com.app.styletap.webtoappconverter.extentions.adjustBottomHeight
import com.app.styletap.webtoappconverter.extentions.changeLocale
import com.app.styletap.webtoappconverter.extentions.customEnableEdgeToEdge
import com.app.styletap.webtoappconverter.extentions.isNetworkAvailable
import com.app.styletap.webtoappconverter.presentations.ui.activities.authorization.LoginActivity
import com.app.styletap.webtoappconverter.presentations.utils.Contants.isShowOnBoarding
import com.app.styletap.webtoappconverter.presentations.utils.Contants.onboarding_native
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper

class OnboardingActivity : AppCompatActivity() {
    lateinit var binding: ActivityOnboardingBinding
    var introCounter = 1
    var fromWhere = "home"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeLocale()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        customEnableEdgeToEdge()

        adjustBottomHeight(binding.container)

        initiView()
        showNativeAd()
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
            PrefHelper.setBoolean(isShowOnBoarding, false)

            val mIntent = Intent(this, LoginActivity::class.java).apply {
                putExtra("from", "splash")
            }
            startActivity(mIntent)
            finish()
        }
    }

    fun showNativeAd(){
        if (isNetworkAvailable() && PrefHelper.getBooleanDefultTrue(onboarding_native) && !PrefHelper.getIsPurchased()){
            binding.adParentLayout.visibility = View.VISIBLE
            binding.nativeLayout.visibility = View.VISIBLE
            binding.shimmerContainer.nativeShimmerView.startShimmer()
            binding.shimmerContainer.nativeShimmerView.visibility = View.VISIBLE
            NativeAdManager(this).loadAndPopulateNativeAdView(this,resources.getString(R.string.onboardingNativeId),binding.adFrame, R.layout.native_ad_medium, binding.shimmerContainer.nativeShimmerView)
        } else {
            binding.adParentLayout.visibility = View.GONE
            binding.shimmerContainer.nativeShimmerView.stopShimmer()
        }
    }

}