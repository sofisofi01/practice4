package com.example.practice4

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.practice4.view_model.CameraViewModel
import com.example.practice4.data.PushUpEntity
import com.example.practice4.ui.CameraPreview
import com.example.practice4.ui.PushUpAnimation
import com.example.practice4.ui.theme.Practice4Theme
import com.example.practice4.view_model.PushUpViewModel
import com.example.practice4.view_model.TrackingMode
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraPermission.launch(Manifest.permission.CAMERA)
        enableEdgeToEdge()
        setContent {
            Practice4Theme {
                PushUpScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PushUpScreen(
    viewModel: PushUpViewModel = hiltViewModel(),
    cameraViewModel: CameraViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.attachCameraViewModel(cameraViewModel)
    }

    val isTracking by viewModel.isTracking.collectAsState()
    val currentCount by viewModel.currentCount.collectAsState()
    val replayIntervals by viewModel.replayIntervals.collectAsState()
    val trackingMode by viewModel.trackingMode.collectAsState()
    val history by viewModel.history.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.pushaps)) }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.sensor))
                    Switch(
                        checked = trackingMode == TrackingMode.CAMERA,
                        onCheckedChange = {
                            viewModel.setMode(if (it) TrackingMode.CAMERA else TrackingMode.SENSOR)
                        },
                        enabled = !isTracking,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text(stringResource(R.string.camera))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (trackingMode == TrackingMode.CAMERA && isTracking) {
                item {
                    CameraPreview(cameraManager = cameraViewModel.cameraManager)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (replayIntervals != null) {
                item {
                    PushUpAnimation(replayIntervals = replayIntervals)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item {
                Text(text = currentCount.toString(), style = MaterialTheme.typography.displayLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (isTracking) viewModel.stopTracking()
                        else viewModel.startTracking()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isTracking) stringResource(R.string.stop) else stringResource(R.string.start))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(stringResource(R.string.history), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(history) { item ->
                HistoryItem(
                    entity = item,
                    onUpdate = { viewModel.updateRecord(it) },
                    onDelete = { viewModel.deleteRecord(it) }
                )
            }
        }
    }
}

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
