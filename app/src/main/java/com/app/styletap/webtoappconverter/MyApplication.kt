package com.app.styletap.webtoappconverter

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        CoroutineScope(Dispatchers.IO).launch {
            FirebaseApp.initializeApp(this@MyApplication)
            FirebaseAnalytics.getInstance(this@MyApplication)
            //FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
        }


    }
}
