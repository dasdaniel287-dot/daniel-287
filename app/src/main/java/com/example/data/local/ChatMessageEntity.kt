package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "user" or "jerves"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSystemProtocol: Boolean = false
)
