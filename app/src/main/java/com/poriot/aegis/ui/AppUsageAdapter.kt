package com.poriot.aegis.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.poriot.aegis.data.AppUsageEntity
import com.poriot.aegis.databinding.ItemAppUsageBinding

class AppUsageAdapter : ListAdapter<AppUsageEntity, AppUsageAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppUsageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemAppUsageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AppUsageEntity) {
            binding.tvAppName.text = item.appName
            val minutes = item.secondsToday / 60
            val limitMinutes = item.dailyLimitSeconds / 60
            binding.tvUsage.text = "${minutes}m / ${limitMinutes}m"
            val percent = (item.secondsToday.toFloat() / item.dailyLimitSeconds * 100).coerceIn(0f, 100f)
            binding.progressUsage.progress = percent.toInt()
            binding.ivWhitelist.visibility = if (item.isWhitelisted) 
                android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AppUsageEntity>() {
        override fun areItemsTheSame(old: AppUsageEntity, new: AppUsageEntity) = 
            old.packageName == new.packageName
        override fun areContentsTheSame(old: AppUsageEntity, new: AppUsageEntity) = 
            old == new
    }
}