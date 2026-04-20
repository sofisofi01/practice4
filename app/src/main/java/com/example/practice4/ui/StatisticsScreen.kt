package com.example.practice4.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.practice4.R
import com.example.practice4.data.PushUpEntity
import com.example.practice4.view_model.PushUpViewModel
import java.util.*

@Composable
fun StatisticsScreen(viewModel: PushUpViewModel) {
    val history by viewModel.history.collectAsState()
    var isChartView by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.statistics),
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    setAlarm(context, hour, minute)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }) {
            Text(stringResource(R.string.set_reminder))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.table_view))
            Switch(
                checked = isChartView,
                onCheckedChange = { isChartView = it },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(stringResource(R.string.chart_view))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isChartView) {
            PushUpChart(history)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
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
}

@Composable
fun PushUpChart(history: List<PushUpEntity>) {
    val data = history.takeLast(10).reversed()
    if (data.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Нет данных для графика")
        }
        return
    }

    val maxCount = data.maxOf { it.count }.toFloat().coerceAtLeast(1f)
    
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .padding(16.dp)
    ) {
        val width = size.width
        val height = size.height
        val spacing = width / (data.size.coerceAtLeast(2) - 1).coerceAtLeast(1)

        val path = Path()
        data.forEachIndexed { index, entity ->
            val x = index * spacing
            val y = height - (entity.count.toFloat() / maxCount * height)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            drawCircle(Color.Blue, radius = 4.dp.toPx(), center = Offset(x, y))
        }

        drawPath(
            path = path,
            color = Color.Blue,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

private fun setAlarm(context: android.content.Context, hour: Int, minute: Int) {
    val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
    val intent = android.content.Intent(context, com.example.practice4.notifications.AlarmReceiver::class.java)
    val pendingIntent = android.app.PendingIntent.getBroadcast(
        context, 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE
    )

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        if (before(Calendar.getInstance())) {
            add(Calendar.DATE, 1)
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    } else {
        alarmManager.setExactAndAllowWhileIdle(
            android.app.AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
    
    android.widget.Toast.makeText(
        context, 
        context.getString(R.string.reminder_set, hour, minute), 
        android.widget.Toast.LENGTH_SHORT
    ).show()
}
