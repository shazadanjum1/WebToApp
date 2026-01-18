package com.app.styletap.webtoappconverter.extentions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
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
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.presentations.utils.Contants.PROCESSING
import com.app.styletap.webtoappconverter.presentations.utils.Contants.READY_TO_DOWNLOAD
import com.app.styletap.webtoappconverter.presentations.utils.DownloadProgressDialog
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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
        isAppearanceLightStatusBars = false
        isAppearanceLightNavigationBars = false
        show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }
}

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

fun isValidPackageName(packageName: String): Boolean {
    val regex = Regex("^[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*){2,}$")
    return regex.matches(packageName)
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


fun TextView.decorateStatus(context: Context, status: String){

    if (status == PROCESSING){
        this.text = resources.getString(R.string.status) +": " + resources.getString(R.string.processing)
        this.setTextColor(ContextCompat.getColor(context,R.color.oringe))
    } else if (status == READY_TO_DOWNLOAD){
        this.text = resources.getString(R.string.status) +": " + resources.getString(R.string.ready_to_download)
        this.setTextColor(ContextCompat.getColor(context,R.color.green))
    } else {
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

fun AppCompatActivity.downloadFile(url: String, appName: String, isShare: Boolean = false) {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "${appName}_$timestamp.zip"

    downloadWithProgress(url, fileName, isShare)

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
