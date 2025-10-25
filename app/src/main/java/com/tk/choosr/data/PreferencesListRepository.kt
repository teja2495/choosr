package com.tk.choosr.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
            gson.fromJson<List<ChoiceList>>(json, type) ?: emptyList()
        }.getOrElse { emptyList() }
    }

    fun saveLists(lists: List<ChoiceList>) {
        val json = gson.toJson(lists)
        prefs.edit().putString(KEY_LISTS_V1, json).apply()
    }

    companion object {
        private const val PREFS_NAME = "choosr_prefs"
        private const val KEY_LISTS_V1 = "lists_v1"
    }
}


