package com.app.styletap.interfaces

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics

object FirebaseAnalyticsUtils {
    fun logEventMessage(message: String) {
        Firebase.analytics.logEvent(message,null)
    }

    fun logEventMessage(message: String,msg2 : String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME,msg2)
        Firebase.analytics.logEvent(message,bundle)
    }
}