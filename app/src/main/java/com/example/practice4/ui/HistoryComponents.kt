package com.example.practice4.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.practice4.R
import com.example.practice4.data.PushUpEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryItem(
    entity: PushUpEntity,
    onUpdate: (PushUpEntity) -> Unit,
    onDelete: (PushUpEntity) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(stringResource(R.string.pushups_count, entity.count), style = MaterialTheme.typography.bodyLarge)
                Text(dateFormat.format(Date(entity.timestamp)), style = MaterialTheme.typography.bodySmall)
            }
            Row {
                TextButton(onClick = { showDialog = true }) { Text(stringResource(R.string.change)) }
                TextButton(onClick = { onDelete(entity) }) { Text(stringResource(R.string.delete)) }
            }
        }
    }

    if (showDialog) {
        EditDialog(
            entity = entity,
            onDismiss = { showDialog = false },
            onConfirm = { newCount -> onUpdate(entity.copy(count = newCount)); showDialog = false }
        )
    }
}

@Composable
fun EditDialog(entity: PushUpEntity, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var text by remember { mutableStateOf(entity.count.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.change_quantity)) },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.quantity)) }
            )
        },
        confirmButton = {
            TextButton(onClick = { text.toIntOrNull()?.let { onConfirm(it) } }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
