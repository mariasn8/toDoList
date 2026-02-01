package com.example.todolist.DB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val creationTime: Long,
    val dueTime: Long,
    val isCompleted: Boolean,
    val category: String,
    val notificationEnabled: Boolean,
    val notificationTimeOffset: Int, // Minutes before deadline
    val attachmentPath: List<String> = emptyList()
)