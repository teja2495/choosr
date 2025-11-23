package com.tk.choosr.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tk.choosr.data.ChoiceList
import com.tk.choosr.data.PreferencesListRepository
import com.tk.choosr.util.ShuffleSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class ListsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PreferencesListRepository(application)
    private val _lists = MutableStateFlow<List<ChoiceList>>(emptyList())
    val lists: StateFlow<List<ChoiceList>> = _lists.asStateFlow()

    private val _avoidPreviousResults = MutableStateFlow(repository.getAvoidPreviousResults())
    val avoidPreviousResults: StateFlow<Boolean> = _avoidPreviousResults.asStateFlow()

    private val shuffleManager = ShuffleSessionManager()

    init {
        _lists.value = repository.loadLists()
    }

    fun setAvoidPreviousResults(value: Boolean) {
        _avoidPreviousResults.value = value
        repository.setAvoidPreviousResults(value)
    }

    fun addList(list: ChoiceList) = updateLists(_lists.value + list)

    fun updateList(updated: ChoiceList) = updateLists(
        _lists.value.map { if (it.id == updated.id) updated else it }
    ).also { shuffleManager.clear(updated.id) }

    fun deleteList(id: String) = updateLists(_lists.value.filterNot { it.id == id })
        .also { shuffleManager.clear(id) }

    fun deleteListDelayed(id: String, delayMillis: Long = 1000) {
        viewModelScope.launch {
            delay(delayMillis)
            deleteList(id)
        }
    }

    fun addItem(listId: String, item: String) {
        val trimmed = item.trim()
        if (trimmed.isEmpty()) return
        val updated = _lists.value.map {
            if (it.id == listId && !it.items.any { s -> s.equals(trimmed, ignoreCase = true) })
                it.copy(items = it.items + trimmed) else it
        }
        updateLists(updated)
        shuffleManager.clear(listId)
    }

    fun removeItem(listId: String, item: String) {
        val updated = _lists.value.map {
            if (it.id == listId) it.copy(items = it.items.filterNot { s -> s == item }) else it
        }
        updateLists(updated)
        shuffleManager.clear(listId)
    }

    fun nextItemIndex(listId: String): Int? {
        val list = _lists.value.firstOrNull { it.id == listId } ?: return null
        if (list.items.isEmpty()) return null
        return shuffleManager.nextIndex(
            listId = listId,
            size = list.items.size,
            avoidPreviousResults = _avoidPreviousResults.value
        )
    }

    private fun updateLists(newLists: List<ChoiceList>) {
        _lists.value = newLists
        viewModelScope.launch { repository.saveLists(newLists) }
    }

    fun exportData(): String {
        return repository.exportData()
    }

    fun importData(json: String): Boolean {
        val success = repository.importData(json)
        if (success) {
            // Reload data from repository
            _lists.value = repository.loadLists()
            _avoidPreviousResults.value = repository.getAvoidPreviousResults()
        }
        return success
    }
}


