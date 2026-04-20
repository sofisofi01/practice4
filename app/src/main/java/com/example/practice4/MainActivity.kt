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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CenterAlignedTopAppBar
import com.example.practice4.notifications.NotificationHelper
import com.example.practice4.ui.HistoryItem
import com.example.practice4.ui.StatisticsScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        NotificationHelper(this).createNotificationChannel()

        cameraPermission.launch(Manifest.permission.CAMERA)
        enableEdgeToEdge()
        setContent {
            Practice4Theme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(if (selectedTab == 0) stringResource(R.string.training) else stringResource(R.string.statistics)) 
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                    label = { Text(stringResource(R.string.training)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text(stringResource(R.string.statistics)) }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> PushUpScreen()
                1 -> StatisticsScreen(hiltViewModel())
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
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
