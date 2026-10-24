package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskPriority {
    CRITICAL, HIGH, NORMAL, LOW
}

enum class TaskCategory {
    DIRECTIVE, CODING, PERSONAL, RESEARCH, SYSTEM
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = TaskCategory.DIRECTIVE.name,
    val priority: String = TaskPriority.HIGH.name,
    val isCompleted: Boolean = false,
    val dueDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
