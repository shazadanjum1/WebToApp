package com.app.styletap.webtoappconverter.extentions

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.doOnLayout
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.DialogDeleteAppBinding
import com.app.styletap.webtoappconverter.presentations.ui.activities.authorization.LoginActivity
import com.app.styletap.webtoappconverter.presentations.utils.Contants.DRAFT_BUNDLE
import com.app.styletap.webtoappconverter.presentations.utils.Contants.ERROR
import com.app.styletap.webtoappconverter.presentations.utils.Contants.PROCESSING
import com.app.styletap.webtoappconverter.presentations.utils.Contants.PROCESSING_BUNDLE
import com.app.styletap.webtoappconverter.presentations.utils.Contants.READY_TO_DOWNLOAD
import com.app.styletap.webtoappconverter.presentations.utils.Contants.READY_TO_DOWNLOAD_BUNDLE
import com.app.styletap.webtoappconverter.presentations.utils.DownloadProgressDialog
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.drawable.toDrawable

import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.core.view.isVisible
import com.app.styletap.ads.InterstitialAdManager
import com.app.styletap.interfaces.FirebaseAnalyticsUtils
import com.app.styletap.interfaces.InterstitialLoadCallback
import com.app.styletap.webtoappconverter.databinding.DialogExitAppBinding
import com.app.styletap.webtoappconverter.databinding.DialogLogoutAppBinding
import com.app.styletap.webtoappconverter.databinding.DialogRateUsBinding
import com.app.styletap.webtoappconverter.presentations.ui.activities.Premium.LifeTimePremiumActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.Premium.SubscriptionAndLifeTimeActivity
import com.app.styletap.webtoappconverter.presentations.ui.activities.Premium.SubscriptionPremiumActivity
import com.app.styletap.webtoappconverter.presentations.utils.Contants.apkdownload_inter
import com.app.styletap.webtoappconverter.presentations.utils.Contants.buildapp_inter
import com.app.styletap.webtoappconverter.presentations.utils.Contants.isIntertialAdshowing
import com.app.styletap.webtoappconverter.presentations.utils.PrefHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(ConnectivityManager::class.java) ?: return false
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
}


fun Activity.customEnableEdgeToEdge() {
    WindowCompat.setDecorFitsSystemWindows(window, false)

    val windowController = ViewCompat.getWindowInsetsController(window.decorView)
    windowController?.apply {
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        isAppearanceLightStatusBars = true
        isAppearanceLightNavigationBars = false
        show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }
}

fun Activity.customEnableEdgeToEdge2(
    statusBarColor: Int = Color.BLUE, // desired color
    darkIcons: Boolean = true
) {
    // Edge-to-edge layout
    WindowCompat.setDecorFitsSystemWindows(window, false)

    // Insets handling
    val rootView = findViewById<View>(R.id.main)
    ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
        val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.setPadding(systemInsets.left, systemInsets.top, systemInsets.right, systemInsets.bottom)
        WindowInsetsCompat.CONSUMED
    }

    // Status bar color
    val finalStatusBarColor = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && darkIcons) {
        manipulateColor(statusBarColor, 0.9f) // slightly darker for white icons
    } else {
        statusBarColor
    }
    window.statusBarColor = finalStatusBarColor

    // Navigation bar color
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.navigationBarColor = Color.TRANSPARENT
    }

    // Status bar icons
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // API 29+
        val controller = ViewCompat.getWindowInsetsController(window.decorView)
        controller?.isAppearanceLightStatusBars = darkIcons
        controller?.isAppearanceLightNavigationBars = false
    }
}

fun manipulateColor(color: Int, factor: Float): Int {
    val a = Color.alpha(color)
    val r = (Color.red(color) * factor).coerceIn(0f, 255f).toInt()
    val g = (Color.green(color) * factor).coerceIn(0f, 255f).toInt()
    val b = (Color.blue(color) * factor).coerceIn(0f, 255f).toInt()
    return Color.argb(a, r, g, b)
}


fun Activity.adjustBottomHeight(layout: ConstraintLayout) {
    val decorView = window.decorView

    // Wait until layout is attached
    decorView.doOnLayout {
        val navBarHeight = ViewCompat.getRootWindowInsets(decorView)
            ?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0

        val params = layout.layoutParams as ConstraintLayout.LayoutParams
        params.bottomMargin = navBarHeight
        layout.layoutParams = params
    }

    // Optional: listen for inset changes (gestures hide/show nav bar)
    ViewCompat.setOnApplyWindowInsetsListener(decorView) { _, insets ->
        val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        val params = layout.layoutParams as ConstraintLayout.LayoutParams
        params.bottomMargin = navBarHeight
        layout.layoutParams = params
        insets
    }
}


/*
fun Activity.adjustBottomHeight(layout: ConstraintLayout){
    val navBarHeight = navBarHeight()

    val params = layout.layoutParams as ConstraintLayout.LayoutParams
    params.bottomMargin = navBarHeight
    layout.layoutParams = params
}

fun Context.navBarHeight(): Int {
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    val height = if (resourceId > 0) {
        resources.getDimensionPixelSize(resourceId)
    } else 0

    return if (isHasNavBarVisible()) height else 0
}

fun Context.isHasNavBarVisible(): Boolean {
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = windowManager.defaultDisplay
    val realMetrics = android.util.DisplayMetrics()
    display.getRealMetrics(realMetrics)

    val displayMetrics = android.util.DisplayMetrics()
    display.getMetrics(displayMetrics)

    val realHeight = realMetrics.heightPixels
    val displayHeight = displayMetrics.heightPixels

    return realHeight > displayHeight
}
 */

fun Activity.adjustTopHeight(layout: ConstraintLayout) {
    ViewCompat.setOnApplyWindowInsetsListener(layout) { view, insets ->

        val statusBarHeight = insets
            .getInsets(WindowInsetsCompat.Type.statusBars())
            .top

        view.setPadding(
            view.paddingLeft,
            statusBarHeight,
            view.paddingRight,
            view.paddingBottom
        )

        insets
    }
}



fun TextView.setClickableText(
    fullText: String,
    clickableText: String,
    @ColorRes clickableColor: Int,
    underline: Boolean = false,
    onClick: () -> Unit
) {
    val spannable = SpannableString(fullText)

    val start = fullText.indexOf(clickableText)
    if (start == -1) return

    val end = start + clickableText.length

    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            onClick()
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.color = ContextCompat.getColor(context, clickableColor)
            ds.isUnderlineText = underline
        }
    }

    spannable.setSpan(
        clickableSpan,
        start,
        end,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    text = spannable
    movementMethod = LinkMovementMethod.getInstance()
    highlightColor = Color.TRANSPARENT
}


fun TextView.setMultiClickableText(
    fullText: String,
    clickableParts: Map<String, () -> Unit>,
    @ColorRes clickableColor: Int,
    underline: Boolean = false
) {
    val spannable = SpannableString(fullText)

    clickableParts.forEach { (text, action) ->
        val start = fullText.indexOf(text)
        if (start == -1) return@forEach

        val end = start + text.length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                action()
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = ContextCompat.getColor(context, clickableColor)
                ds.isUnderlineText = underline
            }
        }

        spannable.setSpan(
            clickableSpan,
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    text = spannable
    movementMethod = LinkMovementMethod.getInstance()
    highlightColor = Color.TRANSPARENT
}


@SuppressLint("ClickableViewAccessibility")
fun EditText.enablePasswordToggle(
    startDrawable: Int,
    eyeOnDrawable: Int,
    eyeOffDrawable: Int
) {
    this.setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_UP) {
            val drawableEnd = 2 // RIGHT drawable index

            val endDrawable = compoundDrawables[drawableEnd]
            if (endDrawable != null &&
                event.rawX >= (right - endDrawable.bounds.width())
            ) {

                val isPasswordHidden =
                    inputType == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                            inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)

                if (isPasswordHidden) {
                    // Show password
                    inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    setCompoundDrawablesWithIntrinsicBounds(
                        startDrawable, 0, eyeOnDrawable, 0
                    )
                } else {
                    // Hide password
                    inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    setCompoundDrawablesWithIntrinsicBounds(
                        startDrawable, 0, eyeOffDrawable, 0
                    )
                }

                setSelection(text.length)
                return@setOnTouchListener true
            }
        }
        false
    }
}


fun Context.addDynamicChips(
    chipGroup: ChipGroup,
    items: ArrayList<String>
) {
    chipGroup.removeAllViews()
    val typeface = ResourcesCompat.getFont(this, R.font.arimo_regular)
    val density = resources.displayMetrics.density

    items.forEach { title ->
        val chip = Chip(this).apply {
            text = title
            isClickable = true
            isCheckable = false

            // Text color
            setTextColor(ContextCompat.getColor(this@addDynamicChips, R.color.chip_text_purple))

            // Background color
            chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(this@addDynamicChips, R.color.chip_bg_purple)
            )

            // ✅ Custom font
            this.typeface = typeface

            // ✅ Corner radius via ShapeAppearanceModel (16dp)
            shapeAppearanceModel = shapeAppearanceModel
                .toBuilder()
                .setAllCornerSizes(16f * resources.displayMetrics.density)
                .build()

            // Optional: remove stroke & icon
            chipStrokeWidth = 0f
            chipIcon = null



            // Padding (dp → px)
            setPadding(
                (2 * resources.displayMetrics.density).toInt(),
                (1 * resources.displayMetrics.density).toInt(),
                (2 * resources.displayMetrics.density).toInt(),
                (1 * resources.displayMetrics.density).toInt()
            )

            // 🔥 Reduce height
            minHeight = 0
            minimumHeight = 0

            textSize = 10f //* density

        }

        chipGroup.addView(chip)
    }
}

/*fun isValidPackageName(packageName: String): Boolean {
    val regex = Regex("^[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*){2,}$")
    return regex.matches(packageName)
}*/

fun isValidPackageName(input: String): String {
    // 1️⃣ replace spaces with dots (keep case)
    var packageName = input
        .trim()
        .replace("\\s+".toRegex(), ".")
        .replace("[^A-Za-z0-9.]".toRegex(), "")

    // 2️⃣ split into parts
    val parts = packageName.split(".").filter { it.isNotEmpty() }.toMutableList()

    // 3️⃣ ensure each part starts with a LETTER (upper or lower)
    for (i in parts.indices) {
        if (!parts[i][0].isLetter()) {
            parts[i] = "A${parts[i]}"
        }
    }

    // 4️⃣ ensure at least 3 parts
    when (parts.size) {
        0 -> parts.addAll(listOf("com", "example", "app"))
        1 -> parts.addAll(listOf("example", "app"))
        2 -> parts.add("app")
    }

    return parts.joinToString(".")
}


fun Long.toFormattedDate(): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        .format(Date(this))
}

fun Any?.toMillis(): Long? {
    return when (this) {
        is Long -> this
        is com.google.firebase.Timestamp -> this.toDate().time
        else -> null
    }
}


fun AppCompatActivity.downloadWithProgress(
    url: String,
    fileName: String,
    isShare: Boolean
) {
    val dialog = DownloadProgressDialog(this)
    val downloadManager =
        getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    val request = DownloadManager.Request(url.toUri())
        .setTitle("Downloading")
        .setDescription("Please wait...")
        .setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            fileName
        )
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)

    val downloadId = downloadManager.enqueue(request)

    dialog.show()

    downloadManager.trackProgress(
        downloadId = downloadId,
        activity = this,
        isShare= isShare,
        onProgress = { dialog.update(it) },
        onFinished = { file ->
            dialog.dismiss()
            if (isShare){
                if (file != null && file.exists()) {
                    shareFile(file)
                }
            }
        }
    )
}

fun DownloadManager.trackProgress(
    downloadId: Long,
    activity: Activity,
    isShare: Boolean,
    onProgress: (Int) -> Unit,
    onFinished: (File?) -> Unit
) {
    Thread {
        var downloading = true
        var downloadedFile: File? = null

        val notificationManager =
            activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "download_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Downloads",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        while (downloading) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = query(query)

            if (cursor.moveToFirst()) {
                val status =
                    cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))

                val downloaded =
                    cursor.getLong(cursor.getColumnIndexOrThrow(
                        DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))

                val total =
                    cursor.getLong(cursor.getColumnIndexOrThrow(
                        DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                if (total > 0) {
                    val progress = ((downloaded * 100) / total).toInt()
                    activity.runOnUiThread { onProgress(progress) }
                }

                if (status == DownloadManager.STATUS_SUCCESSFUL){
                    val localUri =
                        cursor.getString(cursor.getColumnIndexOrThrow(
                            DownloadManager.COLUMN_LOCAL_URI))


                    downloadedFile = localUri?.let {
                        File(Uri.parse(it).path!!)
                    }

                    // Show notification
                    downloadedFile?.let { file ->
                        showDownloadCompleteNotification(activity, file, downloadId.toInt())
                    }


                    activity.runOnUiThread {
                        Toast.makeText(activity, activity.resources.getString(R.string.downloaded), Toast.LENGTH_SHORT).show()
                    }

                    downloading = false
                }

                if (status == DownloadManager.STATUS_FAILED) {
                    activity.runOnUiThread {
                        Toast.makeText(activity, activity.resources.getString(R.string.failed_to_downloading), Toast.LENGTH_SHORT).show()
                    }
                    downloading = false
                }
            }

            cursor.close()
            Thread.sleep(400)
        }

        activity.runOnUiThread { onFinished(downloadedFile) }
    }.start()
}

fun showDownloadCompleteNotification(activity: Activity, file: File, notificationId: Int) {
    val notificationManager =
        activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val channelId = "download_channel"

    // Only create channel on Android O+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Downloads",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Download notifications"
        }
        notificationManager.createNotificationChannel(channel)
    }

    // Use FileProvider for all versions
    val uri = FileProvider.getUriForFile(
        activity,
        "${activity.packageName}.fileprovider",
        file
    )

    val fileIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, activity.contentResolver.getType(uri))
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    val pendingIntent = PendingIntent.getActivity(
        activity,
        0,
        fileIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Build notification
    val notification = NotificationCompat.Builder(activity, channelId)
        .setContentTitle("Download Complete")
        .setContentText(file.name)
        .setSmallIcon(android.R.drawable.stat_sys_download_done) // Required for old Android
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    // Show notification on main thread
    activity.runOnUiThread {
        notificationManager.notify(notificationId, notification)
    }
}




fun DownloadManager.trackProgress1(
    downloadId: Long,
    activity: Activity,
    isShare: Boolean,
    onProgress: (Int) -> Unit,
    onFinished: (File?) -> Unit
) {
    Thread {
        var downloading = true
        var downloadedFile: File? = null

        while (downloading) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = query(query)

            if (cursor.moveToFirst()) {
                val status =
                    cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))

                val downloaded =
                    cursor.getLong(cursor.getColumnIndexOrThrow(
                        DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))

                val total =
                    cursor.getLong(cursor.getColumnIndexOrThrow(
                        DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                if (total > 0) {
                    val progress = ((downloaded * 100) / total).toInt()
                    activity.runOnUiThread { onProgress(progress) }
                }

                if (status == DownloadManager.STATUS_SUCCESSFUL){
                    activity.runOnUiThread {
                        Toast.makeText(activity, activity.resources.getString(R.string.downloaded), Toast.LENGTH_SHORT).show()
                    }

                    if (isShare){
                        val localUri =
                            cursor.getString(cursor.getColumnIndexOrThrow(
                                DownloadManager.COLUMN_LOCAL_URI))

                        downloadedFile = localUri?.let {
                            File(Uri.parse(it).path!!)
                        }
                    }


                    downloading = false
                }

                if (status == DownloadManager.STATUS_FAILED) {
                    activity.runOnUiThread {
                        Toast.makeText(activity, activity.resources.getString(R.string.failed_to_downloading), Toast.LENGTH_SHORT).show()
                    }
                    downloading = false
                }
            }

            cursor.close()
            Thread.sleep(400)
        }

        activity.runOnUiThread { onFinished(downloadedFile) }
    }.start()
}



fun TextView.decorateStatus(context: Context, status: String){

    if (status == PROCESSING){
        this.text = resources.getString(R.string.status) +": " + resources.getString(R.string.processing)
        this.setTextColor(ContextCompat.getColor(context,R.color.oringe))
    } else if (status == READY_TO_DOWNLOAD){
        this.text = resources.getString(R.string.status) +": " + resources.getString(R.string.ready_to_download)
        this.setTextColor(ContextCompat.getColor(context,R.color.green))
    }  else if (status == ERROR){
        this.text = resources.getString(R.string.status) +": " + resources.getString(R.string.something_went_wrong_please_contact_support)
        this.setTextColor(ContextCompat.getColor(context,R.color.red))
    } else if (status == PROCESSING_BUNDLE){
        this.text = resources.getString(R.string.status) +": " + resources.getString(R.string.bundle_processing)
        this.setTextColor(ContextCompat.getColor(context,R.color.oringe))
    } else if (status == READY_TO_DOWNLOAD_BUNDLE){
        this.text = resources.getString(R.string.status) +": " + resources.getString(R.string.bundle_ready_to_download)
        this.setTextColor(ContextCompat.getColor(context,R.color.green))
    }  else if (status == DRAFT_BUNDLE){
        this.text = resources.getString(R.string.status) +": " + resources.getString(R.string.bundle_draft)
        this.setTextColor(ContextCompat.getColor(context,R.color.red))
    }  else {
        this.text = resources.getString(R.string.status) +": " + resources.getString(R.string.draft)
        this.setTextColor(ContextCompat.getColor(context,R.color.red))
    }

}

fun needsStoragePermission(): Boolean {
    return Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
}

fun Context.hasStoragePermission(): Boolean {
    return !needsStoragePermission() ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
}

fun Context.showStorageRationaleDialog(storagePermissionLauncher: ActivityResultLauncher<String>) {
    AlertDialog.Builder(this)
        .setTitle(resources.getString(R.string.storage_permission_required))
        .setMessage(resources.getString(R.string.this_permission_is_needed_to_save_the_downloaded_file_in_your_downloads_folder))
        .setCancelable(false)
        .setPositiveButton(resources.getString(R.string.allow)) { _, _ ->
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        .setNegativeButton(resources.getString(R.string.cancel), null)
        .show()
}

fun AppCompatActivity.downloadFile(url: String, appName: String, isShare: Boolean = false, isApk: Boolean) {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "${appName}_$timestamp.zip"

    if (isApk){
        FirebaseAnalyticsUtils.logEventMessage("export_apk_click")
        if (PrefHelper.getIsPurchased() || !PrefHelper.getBooleanDefultTrue(apkdownload_inter)){
            downloadWithProgress(url, fileName, isShare)
        } else {
            isIntertialAdshowing = true
            InterstitialAdManager(this).loadAndShowAd(
                getString(R.string.downloadApkInterstitialId),
                PrefHelper.getBooleanDefultTrue(buildapp_inter),
                object : InterstitialLoadCallback{
                    override fun onFailedToLoad() {
                        isIntertialAdshowing = false
                        Toast.makeText(this@downloadFile, resources.getString(R.string.failed_to_load_ads_please_try_again), Toast.LENGTH_SHORT).show()
                    }
                    override fun onLoaded() {
                        isIntertialAdshowing = false
                        downloadWithProgress(url, fileName, isShare)
                    }
                }
            )
        }

    } else {
        if (!isShare){
            FirebaseAnalyticsUtils.logEventMessage("export_bundle_click")
        }
        downloadWithProgress(url, fileName, isShare)
    }


}

fun Context.shareFile(file: File) {
    val uri = FileProvider.getUriForFile(
        this,
        "${packageName}.fileprovider",
        file
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "*/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    startActivity(Intent.createChooser(shareIntent, "Share file via"))
}


fun AppCompatImageView.changeIcon(isChecked: Boolean) {
    this.setImageResource(if (isChecked) R.drawable.ic_feature_checked else R.drawable.ic_feature_unchecked)
}

fun Context.showColorPicker(
    onColorPicked: (selectedColor: Int) -> Unit
) {
    ColorPickerDialogBuilder
        .with(this)
        .setTitle(getString(R.string.choose_color))
        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
        .density(12)
        .setOnColorSelectedListener { /* optional */ }
        .setPositiveButton(getString(R.string.choose_color)) { _, selectedColor, _ ->
            onColorPicked(selectedColor)
        }
        .setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }
        .build()
        .show()
}

fun incrementVersion(version: String): String {
    val parts = version.split(".")
    var major = parts.getOrNull(0)?.toIntOrNull() ?: 1
    var minor = parts.getOrNull(1)?.toIntOrNull() ?: 0

    if (minor >= 9) {
        major += 1
        minor = 0
    } else {
        minor += 1
    }

    return "$major.$minor"
}



fun Context.openWhatsApp1(phoneNumber: String) {
    try {
        val cleanNumber = phoneNumber
            .replace("+", "")
            .replace(" ", "")

        val url = "https://wa.me/$cleanNumber"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.setPackage("com.whatsapp") // force WhatsApp

        startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(this, getString(R.string.whatsapp_not_installed), Toast.LENGTH_SHORT).show()
    }
}

fun Context.openWhatsApp(phoneNumber: String) {
    val cleanNumber = phoneNumber
        .replace("+", "")
        .replace(" ", "")

    val uri = Uri.parse("https://wa.me/$cleanNumber")

    val intents = mutableListOf<Intent>()

    // Normal WhatsApp
    val whatsappIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.whatsapp")
    }

    // WhatsApp Business
    val businessIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.whatsapp.w4b")
    }

    //if (whatsappIntent.resolveActivity(packageManager) != null) {
        intents.add(whatsappIntent)
    //}

    //if (businessIntent.resolveActivity(packageManager) != null) {
        intents.add(businessIntent)
    //}

    if (intents.isEmpty()) {
        Toast.makeText(
            this,
            getString(R.string.whatsapp_not_installed),
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    val chooser = Intent.createChooser(
        intents.removeAt(0),
        "Open with WhatsApp"
    )

    chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toTypedArray())

    startActivity(chooser)
}
fun Context.openEmail1(
    email: String,
    subject: String,
    message: String
) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)
        }

        startActivity(Intent.createChooser(intent, "Send email"))
    } catch (_: Exception) {
        Toast.makeText(this, getString(R.string.no_email_app_found), Toast.LENGTH_SHORT).show()
    }
}

fun Context.openEmail(
    email: String,
    subject: String,
    message: String
) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)
            setPackage("com.google.android.gm") // 👈 force Gmail
        }
        startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(this, getString(R.string.no_email_app_found), Toast.LENGTH_SHORT).show()
    }
}

fun Activity.logoutUser() {
    FirebaseAuth.getInstance().signOut()
    Toast.makeText(this, resources.getString(R.string.successfully_logged_out), Toast.LENGTH_SHORT).show()

    val intent = Intent(this, LoginActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
    this.finishAffinity()
}

fun Context.uriToTempFile(uri: Uri): File {
    val inputStream = contentResolver.openInputStream(uri)!!
    val file = File(cacheDir, "icon_${System.currentTimeMillis()}.png")
    file.outputStream().use { output ->
        inputStream.copyTo(output)
    }
    return file
}

fun getInitials(name: String?): String {
    if (name.isNullOrBlank()) return ""

    val parts = name
        .trim()
        .split("\\s+".toRegex())
        .filter { it.isNotEmpty() }

    return when {
        parts.isEmpty() -> ""
        parts.size == 1 -> parts[0].first().uppercaseChar().toString()
        else -> {
            val first = parts[0].first()
            val second = parts[1].first()
            "$first$second".uppercase()
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}


fun getTimeInMillis(value: Any?): Long {
    return when (value) {
        is Long -> value
        is com.google.firebase.Timestamp -> value.toDate().time
        else -> 0L
    }
}


fun Context.openLink(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    } catch (_: Exception){}
}



fun ComponentActivity.withNotificationPermission(onGranted: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                FirebaseAnalyticsUtils.logEventMessage("permission_granted_notification")
                onGranted()
            } else {
                FirebaseAnalyticsUtils.logEventMessage("permission_denied_notification")
                Toast.makeText(this, resources.getString(R.string.notification_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }

        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            onGranted()
        } else {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    } else {
        onGranted()
    }
}

fun Context.showLogoutDialog(
    onClick: () -> Unit
) {
    val dialog = Dialog(this)
    val binding = DialogLogoutAppBinding.inflate(LayoutInflater.from(this))

    dialog.setContentView(binding.root)
    dialog.setCancelable(true)

    dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

    binding.btnCancel.setOnClickListener {
        dialog.dismiss()
    }

    binding.btnDelete.setOnClickListener {
        dialog.dismiss()
        onClick()
    }

    dialog.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    dialog.show()

    val layoutParams = WindowManager.LayoutParams().apply {
        copyFrom(dialog.window?.attributes)
        width = (resources.displayMetrics.widthPixels * 0.90).toInt() // 85% of screen width
        height = WindowManager.LayoutParams.WRAP_CONTENT
    }
    dialog.window?.attributes = layoutParams
}

fun Context.showDeleteDialog(
    onDeleteClick: () -> Unit
) {
    val dialog = Dialog(this)
    val binding = DialogDeleteAppBinding.inflate(LayoutInflater.from(this))

    dialog.setContentView(binding.root)
    dialog.setCancelable(true)

    dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

    binding.btnCancel.setOnClickListener {
        dialog.dismiss()
    }

    binding.btnDelete.setOnClickListener {
        dialog.dismiss()
        onDeleteClick()
    }

    dialog.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    dialog.show()

    val layoutParams = WindowManager.LayoutParams().apply {
        copyFrom(dialog.window?.attributes)
        width = (resources.displayMetrics.widthPixels * 0.90).toInt() // 85% of screen width
        height = WindowManager.LayoutParams.WRAP_CONTENT
    }
    dialog.window?.attributes = layoutParams
}

fun Context.showExitDialog(
    onExitClick: () -> Unit
) {
    val dialog = Dialog(this)
    val binding = DialogExitAppBinding.inflate(LayoutInflater.from(this))

    dialog.setContentView(binding.root)
    dialog.setCancelable(true)

    dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

    binding.btnCancel.setOnClickListener {
        dialog.dismiss()
    }

    binding.btnExit.setOnClickListener {
        dialog.dismiss()
        onExitClick()
    }

    dialog.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    dialog.show()

    val layoutParams = WindowManager.LayoutParams().apply {
        copyFrom(dialog.window?.attributes)
        width = (resources.displayMetrics.widthPixels * 0.90).toInt() // 85% of screen width
        height = WindowManager.LayoutParams.WRAP_CONTENT
    }
    dialog.window?.attributes = layoutParams
}


@SuppressLint("MissingInflatedId")
fun Activity.adLoadingDialog(): android.app.AlertDialog? {
    var alertDialog: android.app.AlertDialog? = null

    try {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setCancelable(false)
        val inflate = LayoutInflater.from(this).inflate(R.layout.dialog_ad_loading, null as ViewGroup?)

        builder.setView(inflate)
        alertDialog = builder.create()

        alertDialog?.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog?.show()
    } catch (_: Exception){}

    return alertDialog
}

fun View.animateViewXaxis() {
    val translationX = ObjectAnimator.ofFloat(this, "translationX", -50f, 50f)
    translationX.duration = 1000
    translationX.repeatCount = ObjectAnimator.INFINITE
    translationX.repeatMode = ObjectAnimator.REVERSE
    translationX.start()
}

fun Activity.proIntent(): Intent {
    //val mIntent = Intent(this, SubscriptionPremiumActivity::class.java)
    val mIntent = Intent(this, SubscriptionAndLifeTimeActivity::class.java)
    return mIntent
}

fun Activity.proLifeTimeIntent(): Intent {
    val mIntent = Intent(this, LifeTimePremiumActivity::class.java)
    return mIntent
}

fun extractNumericValue(formattedPrice: String): Double {
    val numericValue = formattedPrice.replace("[^\\d.]".toRegex(), "")
    return numericValue.toDouble()
}

fun extractPriceUnit(formattedPrice: String): String? {
    val regex = "^[^\\d\\s]+".toRegex()
    val matchResult = regex.find(formattedPrice)
    return matchResult?.value
}

fun calculateMonthlyPriceFromFormattedPrice(formattedPrice: String): Double {
    val yearlyPrice = extractNumericValue(formattedPrice)
    val monthInYear = 52//12
    return yearlyPrice / monthInYear
}

fun Int.dpToPx(context: Context): Int =
    (this * context.resources.displayMetrics.density).toInt()


fun isValidEmail(email: String): Boolean {
    // Allow letters, numbers, dots, underscores, hyphens, plus, and special chars &%#$!
    val emailRegex = "^[A-Za-z0-9+_.&%#\$!-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$"
    return Regex(emailRegex).matches(email)
}

fun Context.shareText(text: String) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_TEXT, text)
    startActivity(Intent.createChooser(shareIntent, "Share text via"))
}

fun Activity.showRateUsDialog() {
    val binding = DialogRateUsBinding.inflate(LayoutInflater.from(this))

    val dialog = androidx.appcompat.app.AlertDialog.Builder(this, R.style.TransparentDialog)
        .setView(binding.root)
        .create()
    var rating = 4
    binding.apply {
        star1.setOnClickListener {
            msgTv.isVisible = false
            rating = 1
            star1.setImageResource(R.drawable.star_filled)
            star2.setImageResource(R.drawable.star_unfilled)
            star3.setImageResource(R.drawable.star_unfilled)
            star4.setImageResource(R.drawable.star_unfilled)
            star5.setImageResource(R.drawable.star_unfilled)
        }
        star2.setOnClickListener {
            msgTv.isVisible = false
            rating = 2
            star1.setImageResource(R.drawable.star_filled)
            star2.setImageResource(R.drawable.star_filled)
            star3.setImageResource(R.drawable.star_unfilled)
            star4.setImageResource(R.drawable.star_unfilled)
            star5.setImageResource(R.drawable.star_unfilled)
        }
        star3.setOnClickListener {
            msgTv.isVisible = false
            rating = 3
            star1.setImageResource(R.drawable.star_filled)
            star2.setImageResource(R.drawable.star_filled)
            star3.setImageResource(R.drawable.star_filled)
            star4.setImageResource(R.drawable.star_unfilled)
            star5.setImageResource(R.drawable.star_unfilled)
        }
        star4.setOnClickListener {
            msgTv.isVisible = false
            rating = 4
            star1.setImageResource(R.drawable.star_filled)
            star2.setImageResource(R.drawable.star_filled)
            star3.setImageResource(R.drawable.star_filled)
            star4.setImageResource(R.drawable.star_filled)
            star5.setImageResource(R.drawable.star_unfilled)
        }
        star5.setOnClickListener {
            msgTv.isVisible = true
            rating = 5
            star1.setImageResource(R.drawable.star_filled)
            star2.setImageResource(R.drawable.star_filled)
            star3.setImageResource(R.drawable.star_filled)
            star4.setImageResource(R.drawable.star_filled)
            star5.setImageResource(R.drawable.star_filled)
        }
    }

    binding.cancelBtn.setOnClickListener{
        dialog.dismiss()
    }

    binding.laterBtn.setOnClickListener{
        dialog.dismiss()
    }

    binding.rateBtn.setOnClickListener {
        if (rating > 3){
            openPlayStore()
        } else {
            openGmail()
        }
        dialog.dismiss()
    }


    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    dialog.show()

    val window = dialog.window
    if (window != null) {
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(window.attributes)
        val displayMetrics = resources.displayMetrics
        layoutParams.width = (displayMetrics.widthPixels * 0.9).toInt()
        window.attributes = layoutParams
    }
}

fun Context.openGmail() {
    try {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(resources.getString(R.string.email)))
            putExtra(Intent.EXTRA_SUBJECT, "Feedback")
        }
        startActivity(emailIntent)
    }catch (_: Exception){}
}

fun Context.openPlayStore() {
    val appPackageName = packageName
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}


fun isValidPassword(password: String): Boolean {
    val regex = Regex(".*[&%#\$@].*")
    return regex.containsMatchIn(password)
}
