package com.app.styletap.webtoappconverter.firebase

import android.app.Activity
import com.app.styletap.interfaces.RemoteConfigCallbackListiner
import com.app.styletap.webtoappconverter.presentations.utils.Contants.apkdownload_inter
import com.app.styletap.webtoappconverter.presentations.utils.Contants.app_open
import com.app.styletap.webtoappconverter.presentations.utils.Contants.buildapp_inter
import com.app.styletap.webtoappconverter.presentations.utils.Contants.createapp_banner
import com.app.styletap.webtoappconverter.presentations.utils.Contants.createapp_native
import com.app.styletap.webtoappconverter.presentations.utils.Contants.generateapp_native
import com.app.styletap.webtoappconverter.presentations.utils.Contants.home_banner
import com.app.styletap.webtoappconverter.presentations.utils.Contants.language_native
import com.app.styletap.webtoappconverter.presentations.utils.Contants.myapps_native
import com.app.styletap.webtoappconverter.presentations.utils.Contants.onboarding_native
import com.app.styletap.webtoappconverter.presentations.utils.Contants.services_native
import com.app.styletap.webtoappconverter.presentations.utils.Contants.splash_inter
import com.app.styletap.webtoappconverter.presentations.utils.Contants.turtorial_native
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteConfigHelper(val activity: Activity) {

    suspend fun firebaseRemoteFetch(
        remoteConfigCallback: RemoteConfigCallbackListiner
    ) = try {
        val result = withContext(Dispatchers.IO) {
            val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build()
            firebaseRemoteConfig.setConfigSettingsAsync(configSettings)

            firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {

                        val prefHelper = PrefHelper(activity)


                        prefHelper.setBoolean(splash_inter, firebaseRemoteConfig.getBoolean(splash_inter))
                        prefHelper.setBoolean(app_open, firebaseRemoteConfig.getBoolean(app_open))
                        prefHelper.setBoolean(language_native, firebaseRemoteConfig.getBoolean(language_native))
                        prefHelper.setBoolean(onboarding_native, firebaseRemoteConfig.getBoolean(onboarding_native))
                        prefHelper.setBoolean(home_banner, firebaseRemoteConfig.getBoolean(home_banner))
                        prefHelper.setBoolean(createapp_native, firebaseRemoteConfig.getBoolean(createapp_native))
                        prefHelper.setBoolean(myapps_native, firebaseRemoteConfig.getBoolean(myapps_native))
                        prefHelper.setBoolean(generateapp_native, firebaseRemoteConfig.getBoolean(generateapp_native))
                        prefHelper.setBoolean(services_native, firebaseRemoteConfig.getBoolean(services_native))
                        prefHelper.setBoolean(turtorial_native, firebaseRemoteConfig.getBoolean(turtorial_native))
                        prefHelper.setBoolean(buildapp_inter, firebaseRemoteConfig.getBoolean(buildapp_inter))
                        prefHelper.setBoolean(apkdownload_inter, firebaseRemoteConfig.getBoolean(apkdownload_inter))

                        prefHelper.setBoolean(createapp_banner, firebaseRemoteConfig.getBoolean(createapp_banner))


                        remoteConfigCallback.onSuccess()
                    } else {
                        remoteConfigCallback.onFailure()
                    }
                }
            true
        }
    } catch (e: Exception) {
        remoteConfigCallback.onFailure()
    }

}