package com.example.todolist.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.DB.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskList(
    tasks: List<Task>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onTaskChecked: (Task, Boolean) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    modifier: Modifier = Modifier
) {

    var hideCompleted by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Total Tasks Counter + Show/Hide Completed Tasks
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Places text on left, button on right
        ) {
            Text(
                text = "Total Tasks: ${tasks.size}",
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = { hideCompleted = !hideCompleted }
            ) {
                Text(text = if (hideCompleted) "Show Completed" else "Hide Completed")
            }
        }

        // 2. Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Search tasks...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true
        )

        val tasksToShow = if (hideCompleted) {
            tasks.filter { !it.isCompleted }
        } else {
            tasks
        }

        // 3. The Task List
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(tasksToShow) { task ->
                TaskItem(
                    task = task,
                    onCheckedChange = { isChecked -> onTaskChecked(task, isChecked) },
                    onDelete = { onDeleteTask(task) },
                    onEdit = { onEditTask(task) }
                )
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onEdit() }, // Clicking the whole card also edits
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onCheckedChange
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    style = if (task.isCompleted)
                        MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough)
                    else MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = "Category: ${task.category}",
                    fontWeight = FontWeight.SemiBold,
                    style = if (task.isCompleted)
                        MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough)
                    else MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Due: ${formatDate(task.dueTime)}",
                    style = if (task.isCompleted)
                        MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough)
                    else MaterialTheme.typography.bodySmall,
                    color = if (task.dueTime < System.currentTimeMillis() && !task.isCompleted) Color.Red else Color.Gray
                )
            }

            // Edit Button
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Task", tint = Color.Gray)
            }

            // Delete Button
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = Color.Gray)
            }
        }
    }
}
// Helper to make the Long timestamp readable
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}