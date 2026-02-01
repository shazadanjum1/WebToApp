package com.app.styletap.webtoappconverter.presentations.utils

import android.content.Context
import android.content.SharedPreferences

class PrefHelper(context: Context) {
    companion object{
        var mPref: SharedPreferences? = null
    }
    var prefHelper: SharedPreferences? = null
    init {
        mPref?.let {
            prefHelper = it
        } ?: run {
            mPref = context.applicationContext.getSharedPreferences("WebToApp", Context.MODE_PRIVATE)
            prefHelper = mPref
        }
    }

    fun setBoolean(key: String, value: Boolean){
        prefHelper?.edit()?.remove(key)?.apply()
        prefHelper?.edit()?.putBoolean(key, value)?.apply()
    }

    fun getBoolean(key: String): Boolean{
        return prefHelper?.getBoolean(key, false) ?: false
    }

    fun getBooleanDefultTrue(key: String): Boolean{
        return prefHelper?.getBoolean(key, true) ?: true
    }

    fun getString(key: String): String? {
        return prefHelper?.getString(key, null)
    }

    fun setString(key: String, value: String) {
        prefHelper?.edit()?.putString(key, value)?.apply()
    }


    fun getString(key: String, defValue: String): String {
        return prefHelper?.getString(key,defValue) ?: defValue
    }

    fun getIsPurchased(): Boolean {
        return prefHelper?.getBoolean("IsPurchased", false) ?: false
    }

    fun setIsPurchased(value: Boolean){
        prefHelper?.edit()?.remove("IsPurchased")?.apply()
        prefHelper?.edit()?.putBoolean("IsPurchased", value)?.apply()
    }


}