package com.example.todolist

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.todolist.DB.Task
import com.example.todolist.FileHelper // Import your new helper
import java.io.File
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

    var attachments by remember(taskToEdit) {
        mutableStateOf(taskToEdit?.attachmentPath ?: emptyList())
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents() // Opens system file picker
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            // Copy file to app storage
            val path = FileHelper.copyUriToInternalStorage(context, uri)
            if (path != null) {
                // Add the new path to our list
                attachments = attachments + path
            }
        }
    }

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
                modifier = Modifier.fillMaxWidth()
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
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

                Divider()

                Text("Attachments:", style = MaterialTheme.typography.titleSmall)

                // List of added attachments
                attachments.forEach { path ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { FileHelper.openFile(context, path) }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Display just the filename
                        Text(
                            text = File(path).name,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        // Delete button
                        IconButton(onClick = {
                            attachments = attachments - path
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove attachment", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                // Add Attachment Button
                Button(
                    onClick = { launcher.launch("*/*") }, // Allow any file type
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Attachment")
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
                            attachmentPath = attachments
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