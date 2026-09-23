package com.victor.ark

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

class AppResolver(private val context: Context) {

    private val nameToPackage: Map<String, String> by lazy { buildIndex() }

    private fun buildIndex(): Map<String, String> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolved = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        val index = mutableMapOf<String, String>()
        for (info in resolved) {
            val label = info.loadLabel(pm).toString().lowercase().trim()
            val packageName = info.activityInfo.packageName
            index[label] = packageName
            index[label.replace(" ", "")] = packageName
        }
        return index
    }

    fun resolve(spokenName: String): String? {
        val normalized = spokenName.lowercase().trim()
        nameToPackage[normalized]?.let { return it }
        nameToPackage[normalized.replace(" ", "")]?.let { return it }

        return nameToPackage.entries.firstOrNull {
            it.key.contains(normalized) || normalized.contains(it.key)
        }?.value
    }

    fun launch(packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        launchIntent?.let { context.startActivity(it) }
    }
}
