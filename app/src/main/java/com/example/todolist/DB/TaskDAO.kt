package com.example.todolist.DB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDAO {
    @Query("SELECT * FROM tasks ORDER BY dueTime ASC")
    fun getAllTasks(): Flow<List<Task>>

    // Task search option
    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%' ORDER BY dueTime ASC")
    fun searchTasks(searchQuery: String): Flow<List<Task>>

    // Create tasks
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertTask(task: Task)

    // Delete tasks
    @Delete
    suspend fun deleteTask(task: Task)

    // Edit task details
    @Update
    suspend fun updateTask(task: Task)
}