package com.app.styletap.webtoappconverter.presentations.ui.activities.language.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.app.styletap.webtoappconverter.R
import com.app.styletap.webtoappconverter.databinding.LanguageItemBinding
import com.app.styletap.webtoappconverter.models.LanguageModel
import com.bumptech.glide.Glide

class LanguageAdapter(
    private val originalList: ArrayList<LanguageModel>,
    private var activity: Activity,
    var selectedLanguage: String,
    val itemClick: (language: LanguageModel) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {

    private var filteredList = ArrayList<LanguageModel>(originalList)
    var selectedPosition = -1

    class ViewHolder(val binding: LanguageItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LanguageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredList[position]

        with(holder.binding) {
            languageTv.text = item.languageName
            cntryNameTv.text = item.ctryName
            Glide.with(activity).load(item.flag).into(flagIv)

            val isSelected = item.languageCode == selectedLanguage

            selectorIv.isVisible = isSelected
            parentViewCL.backgroundTintList =
                if (isSelected) null else ColorStateList.valueOf(Color.WHITE)

            languageTv.setTextColor(
                ContextCompat.getColor(
                    activity,
                    if (isSelected) R.color.white else R.color.black
                )
            )

            cntryNameTv.setTextColor(
                ContextCompat.getColor(
                    activity,
                    if (isSelected) R.color.white_80 else R.color.grey_1
                )
            )

            flagCard.strokeColor = ContextCompat.getColor(
                activity,
                if (isSelected) R.color.blue else R.color.white_80
            )

            parentView.setOnClickListener {
                selectedLanguage = item.languageCode
                notifyDataSetChanged()
                itemClick.invoke(item)
            }
        }
    }

    override fun getItemCount() = filteredList.size

    // 🔍 SEARCH FILTER
    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        filteredList.clear()

        if (query.isEmpty()) {
            filteredList.addAll(originalList)
        } else {
            val search = query.lowercase()
            filteredList.addAll(
                originalList.filter {
                    it.languageName.lowercase().contains(search) ||
                            it.ctryName.lowercase().contains(search)
                }
            )
        }
        notifyDataSetChanged()
    }
}