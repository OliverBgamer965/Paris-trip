package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checklist_items")
data class ChecklistItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // "day1" or "packing"
    val name: String,
    val isChecked: Boolean = false
)
