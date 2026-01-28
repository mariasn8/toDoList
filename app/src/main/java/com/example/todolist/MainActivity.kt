package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.todolist.DB.Task
import com.example.todolist.DB.ToDoDatabase
import com.example.todolist.screens.TaskList
import com.example.todolist.ui.theme.ToDoListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize DB and ViewModel
        val database = ToDoDatabase.getDatabase(this)
        val viewModelFactory = ToDoViewModelFactory(database.taskDao())
        val viewModel = ViewModelProvider(this, viewModelFactory)[ToDoViewModel::class.java]

        setContent {
            ToDoListTheme {
                // --- STATE VARIABLES ---
                var showDialog by remember { mutableStateOf(false) }
                var searchQuery by remember { mutableStateOf("") }

                var taskToEdit by remember { mutableStateOf<Task?>(null) }

                // Observe the list of tasks from the database
                val tasks by viewModel.tasks.observeAsState(initial = emptyList())

                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            taskToEdit = null
                            showDialog = true
                        }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Task")
                        }
                    }
                ) { innerPadding ->

                    TaskList(
                        tasks = tasks,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { query ->
                            searchQuery = query
                            viewModel.search(query)
                        },
                        onTaskChecked = { task, isChecked ->
                            viewModel.updateTask(task.copy(isCompleted = isChecked))
                        },
                        onDeleteTask = { task -> viewModel.deleteTask(task) },
                        onEditTask = { task ->
                            taskToEdit = task
                            showDialog = true
                        },
                        modifier = Modifier.padding(innerPadding)
                    )


                    // Dialog Logic
                    if (showDialog) {
                        AddTask(
                            taskToEdit = taskToEdit, // Pass the selected task (or null)
                            onDismiss = { showDialog = false },
                            onSaveTask = { task ->
                                if (task.id == 0) {
                                    // ID is 0, NEW task
                                    viewModel.addTask(task)
                                } else {
                                    // ID is not 0, update EXISTING task
                                    viewModel.updateTask(task)
                                }
                                showDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}