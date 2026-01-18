package com.app.styletap.webtoappconverter.presentations.ui.activities.myApps.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.MyAppsItemBinding
import com.app.styletap.webtoappconverter.extentions.toFormattedDate
import com.app.styletap.webtoappconverter.extentions.toMillis
import com.app.styletap.webtoappconverter.models.AppModel
import com.app.styletap.webtoappconverter.presentations.utils.Contants.DRAFT
import com.app.styletap.webtoappconverter.presentations.utils.Contants.PROCESSING
import com.app.styletap.webtoappconverter.presentations.utils.Contants.READY_TO_DOWNLOAD
import java.lang.reflect.Method
import androidx.core.view.size
import androidx.core.view.get
import androidx.core.view.isVisible
import com.app.styletap.webtoappconverter.extentions.decorateStatus
import com.app.styletap.webtoappconverter.extentions.isNetworkAvailable
import com.app.styletap.webtoappconverter.presentations.ui.activities.myApps.EditAppActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.myApps.MyAppsActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.myApps.ViewAppDetailsActivity
import com.bumptech.glide.Glide

class MyAppsAdapter(
    val activity: Activity,
    private val apps: List<AppModel>,
    private val onItemClick: (AppModel) -> Unit
) : RecyclerView.Adapter<MyAppsAdapter.AppViewHolder>() {

    inner class AppViewHolder(
        val binding: MyAppsItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = MyAppsItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]

        with(holder.binding) {
            appNameTv.text = app.appName ?: "Unnamed App"
            versionTv.text = "${versionTv.context.resources.getString(R.string.version)}: " + app.appVersion ?: "1"
            val millis = app.updatedAt.toMillis()
            dateTv.text =  "${versionTv.context.resources.getString(R.string.version)}: ${millis?.toFormattedDate()}"

            // Status
            statusTv.decorateStatus(statusTv.context,app.status ?: DRAFT)

            if (app.status == READY_TO_DOWNLOAD){
                optionMenuBtn.isVisible = true
            } else {
                optionMenuBtn.isVisible = false
            }

            app.appIconUrl?.let {
                Glide.with(activity.applicationContext)
                    .load(app.appIconUrl)
                    .circleCrop()
                    .into(apkIV)
            }

            cardView.setOnClickListener {
                app.id?.let { appId ->
                    if (!activity.isNetworkAvailable()){
                        Toast.makeText(activity, activity.resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                    } else {
                        val mIntent = Intent(activity, ViewAppDetailsActivity::class.java).apply {
                            putExtra("appId", appId)
                        }
                        activity.startActivity(mIntent)
                    }
                }
            }

            optionMenuBtn.setOnClickListener {
                showPopupMenu(it, app, activity)
            }
        }
    }

    override fun getItemCount(): Int = apps.size

    private fun showPopupMenu(view: View, app: AppModel, activity1: Activity) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.my_apps_option_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_view_details -> {
                    app.id?.let { appId ->
                        if (!activity1.isNetworkAvailable()){
                            Toast.makeText(activity1, activity1.resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                        } else {
                            val mIntent = Intent(activity1, ViewAppDetailsActivity::class.java).apply {
                                putExtra("appId", appId)
                            }
                            activity1.startActivity(mIntent)
                        }
                    }
                    true
                }
                R.id.action_edit_app -> {
                    app.id?.let { appId ->
                        if (!activity1.isNetworkAvailable()){
                            Toast.makeText(activity1, activity1.resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                        } else {
                            val mIntent = Intent(activity1, EditAppActivity::class.java).apply {
                                putExtra("appId", appId)
                            }
                            activity1.startActivity(mIntent)
                        }
                    }
                    true
                }
                /*R.id.action_download_aab -> {
                    true
                }*/
                R.id.action_download_apk -> {
                    app.apkUrl?.let { appUrl->
                        if (!activity1.isNetworkAvailable()){
                            Toast.makeText(activity1, activity1.resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                        } else {
                            if (activity1 is MyAppsActivity){
                                activity1.startDownload(appUrl, app.appName ?: "app")
                            }
                        }
                    }
                    true
                }
                R.id.action_delete_app -> {
                    app.id?.let { appId->
                        if (!activity1.isNetworkAvailable()){
                            Toast.makeText(activity1, activity1.resources.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                        } else {
                            if (activity1 is MyAppsActivity){
                                activity1.deleteApp(appId)
                            }
                        }
                    }
                    true
                }
                else -> false
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // API 29+
            popup.setForceShowIcon(true)
        } else {
            try {
                val fields = popup.javaClass.declaredFields
                for (field in fields) {
                    if (field.name == "mPopup") {
                        field.isAccessible = true
                        val menuPopupHelper = field.get(popup)
                        val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                        val setForceIcons: Method = classPopupHelper.getMethod(
                            "setForceShowIcon",
                            Boolean::class.javaPrimitiveType
                        )
                        setForceIcons.invoke(menuPopupHelper, true)
                        break
                    }
                }
            } catch (_: Exception) { }
        }


        for (i in 0 until popup.menu.size) {
            val menuItem = popup.menu[i]
            val titleString = menuItem.title.toString()

            val colorRes = if (titleString == view.context.getString(R.string.delete_app)) {
                R.color.red_2
            } else {
                R.color.black_1
            }

            val color = ContextCompat.getColor(view.context, colorRes)

            val span = SpannableString(titleString)
            span.setSpan(ForegroundColorSpan(color), 0, span.length, 0)
            menuItem.title = span
        }

        popup.show()
    }



}
