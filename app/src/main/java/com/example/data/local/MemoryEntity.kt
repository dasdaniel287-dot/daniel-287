package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val tag: String = "CORE", // e.g., USER_INFO, PREFERENCE, PROJECT, NOTE
    val createdAt: Long = System.currentTimeMillis()
)
