package com.example.practice4.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.practice4.R
import com.example.practice4.data.PushUpEntity

@Composable
fun PushUpChart(history: List<PushUpEntity>) {
    val data = history.takeLast(10).reversed()
    if (data.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_data_chart))
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
