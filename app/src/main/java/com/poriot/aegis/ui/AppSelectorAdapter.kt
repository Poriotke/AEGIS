package com.poriot.aegis.ui

import android.content.pm.ApplicationInfo
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.poriot.aegis.databinding.ItemAppSelectorBinding

class AppSelectorAdapter(
    private val apps: List<ApplicationInfo>,
    private val selectedApps: MutableSet<String>,
    private val onToggle: (String, Boolean) -> Unit
) : RecyclerView.Adapter<AppSelectorAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppSelectorBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(apps[position])
    }

    override fun getItemCount() = apps.size

    inner class ViewHolder(private val binding: ItemAppSelectorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(app: ApplicationInfo) {
            val pm = binding.root.context.packageManager
            val label = pm.getApplicationLabel(app).toString()
            val pkg = app.packageName

            binding.tvAppName.text = label
            binding.tvPackage.text = pkg
            binding.checkbox.isChecked = pkg in selectedApps

            binding.root.setOnClickListener {
                val isNowSelected = !binding.checkbox.isChecked
                binding.checkbox.isChecked = isNowSelected
                onToggle(pkg, isNowSelected)
            }
        }
    }
}