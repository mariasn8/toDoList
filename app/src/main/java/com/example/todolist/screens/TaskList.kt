package com.example.todolist.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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

    var hideCompleted by rememberSaveable { mutableStateOf(false) }

    val allCategories = rememberSaveable (tasks) {
        tasks.map { it.category }.distinct().sorted()
    }
    var selectedCategories by rememberSaveable {
        mutableStateOf(emptySet<String>())
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Total Tasks Counter + Show/Hide Completed Tasks
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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

        if (allCategories.isNotEmpty()) {
            Text(
                text = "Filter by Category:",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allCategories) { category ->
                    val isSelected = category in selectedCategories
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategories = if (isSelected) {
                                selectedCategories - category // Unselect
                            } else {
                                selectedCategories + category // Select
                            }
                        },
                        label = { Text(category) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else null
                    )
                }
            }
        }

        val tasksToShow = remember(tasks, hideCompleted, searchQuery, selectedCategories) {
            tasks.filter { task ->
                // 1. Check Hide Completed
                val matchesCompletion = !hideCompleted || !task.isCompleted

                // 2. Check Search Query (Title or Description)
                val matchesSearch = task.title.contains(searchQuery, ignoreCase = true) ||
                        task.description.contains(searchQuery, ignoreCase = true)

                // 3. Check Category (If no categories selected, show alld)
                val matchesCategory = selectedCategories.isEmpty() || task.category in selectedCategories

                matchesCompletion && matchesSearch && matchesCategory
            }
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
            .padding(horizontal = 12.dp, vertical = 8.dp),
            //.clickable { onEdit() }, // Clicking the whole card also edits
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
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
                    text = "Description: ${task.description}",
                    fontWeight = FontWeight.SemiBold,
                    style = if (task.isCompleted)
                        MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough)
                    else MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Created: ${formatDate(task.creationTime)}",
                    fontWeight = FontWeight.SemiBold,
                    style = if (task.isCompleted)
                        MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough)
                    else MaterialTheme.typography.bodySmall
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Due: ${formatDate(task.dueTime)}",
                        fontWeight = FontWeight.SemiBold,
                        style = if (task.isCompleted)
                            MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough)
                        else MaterialTheme.typography.bodySmall,
                        color = if (task.dueTime < System.currentTimeMillis() && !task.isCompleted) Color.Red else Color.DarkGray
                    )

                    if (task.attachmentPath.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attachments",
                            modifier = Modifier.size(16.dp)
                        )
                        // Show the count of files
                        Text(
                            text = "(${task.attachmentPath.size})",
                            fontWeight = FontWeight.SemiBold,
                            style = if (task.isCompleted)
                                MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough)
                            else MaterialTheme.typography.bodySmall
                        )
                    }

                }
            }

            // Edit Button
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit,
                    contentDescription = "Edit Task",
                    tint = Color.Gray)
            }

            // Delete Button
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = Color.Gray)
            }
        }
    }
}
// Helper to make the Long timestamp readable
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}