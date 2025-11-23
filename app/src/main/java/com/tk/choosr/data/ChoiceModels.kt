package com.tk.choosr.data

import java.util.UUID

data class ChoiceList(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val items: List<String> = emptyList(),
    val emoji: String? = null,
    val colorArgb: Long? = null
)

data class ExportData(
    val lists: List<ChoiceList>,
    val avoidPreviousResults: Boolean,
    val viewType: String? = null
)


