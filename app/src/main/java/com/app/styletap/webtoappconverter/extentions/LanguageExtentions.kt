package com.app.styletap.webtoappconverter.extentions

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.models.LanguageModel
import com.app.styletap.webtoappconverter.presentations.utils.Contants.languageCode
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper
import java.util.Locale

fun getLanguageData(): ArrayList<LanguageModel> {
    val languageList = ArrayList<LanguageModel>()
    languageList.add(LanguageModel(R.drawable.flag_eng, "English", "English","en", false))
    languageList.add(LanguageModel(R.drawable.flag_spanish, "Spanish", "Español","es", false))
    languageList.add(LanguageModel(R.drawable.flag_french, "French", "Français","fr", false))
    languageList.add(LanguageModel(R.drawable.flag_german, "German", "Deutsch","de", false))
    languageList.add(LanguageModel(R.drawable.flag_italian, "Italian", "Italiano","it", false))
    languageList.add(LanguageModel(R.drawable.flag_portuguese, "Portuguese", "Português","pt", false))
    languageList.add(LanguageModel(R.drawable.flag_portuguese_br, "Portuguese (BR)", "Português (BR)","pt-rBR", false))
    languageList.add(LanguageModel(R.drawable.flag_chinese, "Chinese", "中文","zh", false))
    languageList.add(LanguageModel(R.drawable.flag_japanese, "Japanese", "日本語","ja", false))
    languageList.add(LanguageModel(R.drawable.flag_korean, "Korean", "한국어","ko", false))
    languageList.add(LanguageModel(R.drawable.flag_arabic, "Arabic", "العربية","ar", false))
    languageList.add(LanguageModel(R.drawable.flag_hindi, "Hindi", "हिन्दी","hi", false))
    languageList.add(LanguageModel(R.drawable.flag_russian, "Russian", "Русский","ru", false))
    languageList.add(LanguageModel(R.drawable.flag_turkish, "Turkish", "Türkçe","tr", false))
    languageList.add(LanguageModel(R.drawable.flag_dutch, "Dutch", "Nederlands","nl-rNL", false))
    languageList.add(LanguageModel(R.drawable.flag_polish, "Polish", "Polski","pl", false))
    languageList.add(LanguageModel(R.drawable.flag_swedish, "Swedish", "Svenska","sv", false))
    languageList.add(LanguageModel(R.drawable.flag_indonesian, "Indonesian", "Bahasa Indonesia","in", false))

    return languageList
}


fun Activity.changeLocale(){
    val selectedLanguageCode = PrefHelper(applicationContext).getString(languageCode, "en").toString()
    setLocale(this, selectedLanguageCode){}
}

fun Activity.changeToDeviceLocale() {
    val deviceLocale = Locale.getDefault()              // e.g. ur_PK, en_US
    val deviceLanguageTag = deviceLocale.toLanguageTag() // "ur-PK", "en-US"
    setLocale(this, deviceLanguageTag) {}
}


fun setLocale1(activity: Activity, languageCode: String, onLocaleChanged: () -> Unit) {

    val config = activity.resources.configuration
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
        config.setLocale(locale)
    else
        config.locale = locale

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        activity.createConfigurationContext(config)

    activity.resources.updateConfiguration(config, activity.resources.displayMetrics)

    onLocaleChanged.invoke()
}

fun setLocale(activity: Activity, languageCode: String, onLocaleChanged: () -> Unit) {
    val locale = Locale.forLanguageTag(languageCode)
    Locale.setDefault(locale)

    val config = Configuration(activity.resources.configuration)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        config.setLocale(locale)
        val context: Context = activity.createConfigurationContext(config)
        activity.resources.updateConfiguration(context.resources.configuration, context.resources.displayMetrics)
    } else {
        config.locale = locale
        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }

    onLocaleChanged.invoke()
}