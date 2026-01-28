package com.example.todolist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.todolist.DB.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTask(
    taskToEdit: Task? = null, // The task to edit (null if creating new)
    onDismiss: () -> Unit,
    onSaveTask: (Task) -> Unit
) {
    // Initialize state with existing task data if available, otherwise empty/default
    var title by remember(taskToEdit) { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember(taskToEdit) { mutableStateOf(taskToEdit?.description ?: "") }
    var category by remember(taskToEdit) { mutableStateOf(taskToEdit?.category ?: "") }

    // Default to tomorrow if new, or use existing due time
    var dueTime by remember(taskToEdit) {
        mutableLongStateOf(taskToEdit?.dueTime ?: (System.currentTimeMillis() + 86400000))
    }

    var isNotificationEnabled by remember(taskToEdit) {
        mutableStateOf(taskToEdit?.notificationEnabled ?: false)
    }
    var notificationOffset by remember(taskToEdit) {
        mutableStateOf(taskToEdit?.notificationTimeOffset?.toString() ?: "10")
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Date Picker Dialog Logic
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueTime)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dueTime = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (taskToEdit == null) "New Task" else "Edit Task") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true
                )

                Divider()

                // Date Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Due: ${dateFormatter.format(Date(dueTime))}")
                    TextButton(onClick = { showDatePicker = true }) { Text("Pick Date") }
                }

                // Notifications
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Notification")
                    Switch(checked = isNotificationEnabled, onCheckedChange = { isNotificationEnabled = it })
                }

                if (isNotificationEnabled) {
                    OutlinedTextField(
                        value = notificationOffset,
                        onValueChange = { if (it.all { char -> char.isDigit() }) notificationOffset = it },
                        label = { Text("Minutes before") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val taskToSave = Task(
                            // Important: If editing, keep the old ID. If new, use 0 (auto-generate).
                            id = taskToEdit?.id ?: 0,

                            title = title,
                            description = description,
                            category = category.ifBlank { "General" },

                            // Keep original creation time if editing
                            creationTime = taskToEdit?.creationTime ?: System.currentTimeMillis(),

                            dueTime = dueTime,
                            isCompleted = taskToEdit?.isCompleted ?: false,
                            notificationEnabled = isNotificationEnabled,
                            notificationTimeOffset = notificationOffset.toIntOrNull() ?: 10,
                            attachmentPath = taskToEdit?.attachmentPath ?: emptyList()
                        )
                        onSaveTask(taskToSave)
                        onDismiss()
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}