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
import com.example.todolist.DB.*
import com.example.todolist.screens.*
import com.example.todolist.ui.theme.ToDoListTheme
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.saveable.rememberSaveable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Create Channel
        createNotificationChannel()

        enableEdgeToEdge()

        // 2. Initialize DB and ViewModel
        val database = ToDoDatabase.getDatabase(this)
        val viewModelFactory = ToDoViewModelFactory(database.taskDao())
        val viewModel = ViewModelProvider(this, viewModelFactory)[ToDoViewModel::class.java]

        setContent {
            ToDoListTheme {
                // State variables
                var showDialog by rememberSaveable { mutableStateOf(false) }
                var searchQuery by rememberSaveable { mutableStateOf("") }

                var taskToEdit by rememberSaveable { mutableStateOf<Task?>(null) }

                // Observe the list of tasks from the database
                val tasks by viewModel.tasks.observeAsState(initial = emptyList())

                val context = LocalContext.current

                LaunchedEffect(intent) {
                    val taskIdToOpen = intent.getIntExtra("TASK_ID_TO_OPEN", -1)
                    if (taskIdToOpen != -1) {
                        // Logic to find task by ID and open AddTask dialog
                        // You usually need to ask your ViewModel to find the task
                        viewModel.getTaskById(taskIdToOpen) { task ->
                            // Setup your state to open the edit dialog
                            taskToEdit = task
                            showDialog = true
                        }
                    }
                }

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
                        onDeleteTask = { task ->
                            viewModel.deleteTask(task)
                            NotificationScheduler.cancelNotification(context, task) },
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
                                    NotificationScheduler.scheduleNotification(context, task)
                                } else {
                                    // ID is not 0, update EXISTING task
                                    viewModel.updateTask(task)
                                    NotificationScheduler.scheduleNotification(context, task)
                                }
                                showDialog = false
                            }
                        )
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Notifications"
            val descriptionText = "Channel for ToDo List reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("task_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}