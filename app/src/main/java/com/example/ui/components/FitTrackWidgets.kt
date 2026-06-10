package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FitnessGold

@Composable
fun MetricCircle(
    value: Int,
    target: Int,
    label: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 12.dp
) {
    val progress = if (target > 0) value.toFloat() / target.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(140.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background ring
            drawCircle(
                color = color.copy(alpha = 0.15f),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            // Foreground animated progress
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$value",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            )
            Text(
                text = "/ $target $unit",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }
    }
}

@Composable
fun NutrientMiniRow(
    label: String,
    value: Int,
    target: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (target > 0) value.toFloat() / target.toFloat() else 0f
    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "${value}g / ${target}g",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(color.copy(alpha = 0.1f), RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color.Transparent
        )
    }
}

@Composable
fun InteractiveLineChart(
    dataPoints: List<Float>,
    labels: List<String>,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 160.dp
) {
    if (dataPoints.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Add measurements to generate historical progress analytics",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
        return;
    }

    val maxVal = dataPoints.maxOrNull() ?: 100f
    val minVal = dataPoints.minOrNull() ?: 0f
    val range = if (maxVal == minVal) 10f else maxVal - minVal

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            val width = size.width
            val canvasHeight = size.height
            val spacing = width / (dataPoints.size - 1)

            // Draw horizontal reference gridlines
            val linesCount = 4
            for (i in 0 until linesCount) {
                val y = canvasHeight * (i.toFloat() / (linesCount - 1))
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.15f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Map dataPoints to canvas points
            val points = dataPoints.mapIndexed { idx, point ->
                val x = idx * spacing
                val y = canvasHeight - ((point - minVal) / range) * canvasHeight
                Offset(x, y)
            }

            // Draw path line
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = color,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 3.dp.toPx()
                )
            }

            // Draw filled gradient area
            val fillPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(points.first().x, canvasHeight)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, canvasHeight)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = 0.35f), Color.Transparent),
                    startY = 0f,
                    endY = canvasHeight
                )
            )

            // Draw points dots
            points.forEach { point ->
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = color,
                    radius = 3.dp.toPx(),
                    center = point
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Draw horizontal labels row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun AchievementPill(
    title: String,
    description: String,
    requiredXP: Int,
    currentXP: Int,
    modifier: Modifier = Modifier
) {
    val unlocked = currentXP >= requiredXP
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Achievement icon",
                tint = if (unlocked) FitnessGold else Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(36.dp)
                    .padding(end = 8.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (unlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                text = "${requiredXP}XP",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (unlocked) MaterialTheme.colorScheme.primary else Color.Gray
                )
            )
        }
    }
}
