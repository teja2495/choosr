package com.tk.choosr.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Locale
import java.util.UUID

class PreferencesListRepository(
    context: Context,
    private val gson: Gson = Gson(),
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadLists(): List<ChoiceList> {
        val json = prefs.getString(KEY_LISTS_V1, null) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<ChoiceList>>() {}.type
            val parsed = gson.fromJson<List<ChoiceList>>(json, type) ?: emptyList()
            sanitizeLists(parsed)
        }.getOrElse { emptyList() }
    }

    fun saveLists(lists: List<ChoiceList>) {
        val json = gson.toJson(lists)
        prefs.edit().putString(KEY_LISTS_V1, json).apply()
    }

    fun getAvoidPreviousResults(): Boolean {
        return prefs.getBoolean(KEY_AVOID_PREVIOUS_RESULTS, true)
    }

    fun setAvoidPreviousResults(value: Boolean) {
        prefs.edit().putBoolean(KEY_AVOID_PREVIOUS_RESULTS, value).apply()
    }

    fun getViewType(): String {
        return prefs.getString(KEY_VIEW_TYPE, "grid") ?: "grid"
    }

    fun setViewType(value: String) {
        prefs.edit().putString(KEY_VIEW_TYPE, value).apply()
    }

    fun exportData(): String {
        val exportData = ExportData(
            lists = loadLists(),
            avoidPreviousResults = getAvoidPreviousResults(),
            viewType = getViewType()
        )
        return gson.toJson(exportData)
    }

    fun importData(json: String): Boolean {
        return runCatching {
            val type = object : TypeToken<ExportData>() {}.type
            val exportData: ExportData = gson.fromJson(json, type) ?: return false
            val sanitizedLists = sanitizeLists(exportData.lists)

            // Replace all data with imported data
            saveLists(sanitizedLists)
            setAvoidPreviousResults(exportData.avoidPreviousResults)
            setViewType(exportData.viewType ?: "grid")
            true
        }.getOrElse { false }
    }

    private fun sanitizeLists(rawLists: List<ChoiceList>): List<ChoiceList> {
        val usedIds = mutableSetOf<String>()
        return rawLists.mapNotNull { list ->
            runCatching {
                val trimmedName = list.name?.trim().orEmpty()
                if (trimmedName.isEmpty()) return@mapNotNull null

                val sanitizedItems = sanitizeItems(list.items)
                val sanitizedEmoji = list.emoji?.trim()?.takeIf { it.isNotEmpty() }
                val sanitizedColor = list.colorArgb?.takeIf { it in MIN_COLOR_VALUE..MAX_COLOR_VALUE }
                val stableId = ensureUniqueId(list.id, usedIds)

                ChoiceList(
                    id = stableId,
                    name = trimmedName,
                    items = sanitizedItems,
                    emoji = sanitizedEmoji,
                    colorArgb = sanitizedColor
                )
            }.getOrNull()
        }
    }

    private fun sanitizeItems(items: List<String>?): List<String> {
        val seen = mutableSetOf<String>()
        val sanitized = mutableListOf<String>()
        items.orEmpty().forEach { raw ->
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return@forEach
            val key = trimmed.lowercase(Locale.ROOT)
            if (seen.add(key)) {
                sanitized += trimmed
            }
        }
        return sanitized
    }

    private fun ensureUniqueId(rawId: String?, usedIds: MutableSet<String>): String {
        if (!rawId.isNullOrBlank() && usedIds.add(rawId)) {
            return rawId
        }
        var newId: String
        do {
            newId = UUID.randomUUID().toString()
        } while (!usedIds.add(newId))
        return newId
    }

    companion object {
        private const val PREFS_NAME = "choosr_prefs"
        private const val KEY_LISTS_V1 = "lists_v1"
        private const val KEY_AVOID_PREVIOUS_RESULTS = "avoid_previous_results"
        private const val KEY_VIEW_TYPE = "view_type"
        private const val MIN_COLOR_VALUE = 0x00000000L
        private const val MAX_COLOR_VALUE = 0xFFFFFFFFL
    }
}


