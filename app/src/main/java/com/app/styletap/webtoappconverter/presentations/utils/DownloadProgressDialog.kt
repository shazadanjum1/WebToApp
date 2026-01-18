package com.app.styletap.webtoappconverter.presentations.utils

import android.app.Activity
import android.app.Dialog
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.app.styletap.webtoappconverter.R

class DownloadProgressDialog(private val activity: Activity) {

    private val dialog: Dialog = Dialog(activity)
    private val progressBar: ProgressBar
    private val progressText: TextView

    init {
        dialog.setContentView(R.layout.dialog_download)
        dialog.setCancelable(false)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        progressBar = dialog.findViewById(R.id.progressBar)
        progressText = dialog.findViewById(R.id.progressTag)
    }

    fun show() {
        progressBar.progress = 0
        progressText.text = activity.getString(R.string.downloading)
        dialog.show()
    }

    fun update(progress: Int) {
        progressBar.progress = progress
        progressText.text =
            "${activity.getString(R.string.downloading)} $progress%"
    }

    fun dismiss() {
        if (dialog.isShowing) dialog.dismiss()
    }
}
