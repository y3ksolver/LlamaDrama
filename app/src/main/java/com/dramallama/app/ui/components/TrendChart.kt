package com.dramallama.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class TrendDataPoint(
    val date: LocalDate,
    val value: Int  // 1 = Low, 2 = Okay, 3 = Good
)

@Composable
fun TrendChart(
    title: String,
    emoji: String,
    dataPoints: List<TrendDataPoint>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) {
        EmptyTrendChart(title = title, emoji = emoji, modifier = modifier)
        return
    }

    val sortedPoints = remember(dataPoints) { 
        dataPoints.sortedBy { it.date } 
    }
    
    // Calculate date range
    val minDate = sortedPoints.first().date
    val maxDate = sortedPoints.last().date
    val totalDays = ChronoUnit.DAYS.between(minDate, maxDate).coerceAtLeast(1)
    
    // Format for axis labels
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM") }
    
    // Get unique months for labels
    val monthLabels = remember(sortedPoints) {
        sortedPoints.map { it.date.withDayOfMonth(1) }
            .distinct()
            .take(4) // Show max 4 month labels
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        // Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${sortedPoints.size} entries",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Chart area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            // Y-axis labels
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Good",
                    style = MaterialTheme.typography.labelSmall,
                    color = SentimentColors.levelGood
                )
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    "Okay",
                    style = MaterialTheme.typography.labelSmall,
                    color = SentimentColors.levelOkay
                )
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    "Low",
                    style = MaterialTheme.typography.labelSmall,
                    color = SentimentColors.levelLow
                )
            }
            
            // Extract color for use inside Canvas (non-composable context)
            val lineColor = MaterialTheme.colorScheme.primary
            
            // Canvas for the chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(start = 40.dp)
            ) {
                val chartWidth = size.width
                val chartHeight = size.height
                val paddingTop = 8.dp.toPx()
                val paddingBottom = 8.dp.toPx()
                val usableHeight = chartHeight - paddingTop - paddingBottom
                
                // Draw horizontal grid lines
                val gridColor = Color.Gray.copy(alpha = 0.2f)
                for (i in 0..2) {
                    val y = paddingTop + (usableHeight * i / 2)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                
                if (sortedPoints.size == 1) {
                    // Single point - just draw a dot
                    val point = sortedPoints.first()
                    val x = chartWidth / 2
                    val y = paddingTop + usableHeight * (3 - point.value) / 2
                    val color = SentimentColors.getColorForLevel(point.value)
                    drawCircle(
                        color = color,
                        radius = 6.dp.toPx(),
                        center = Offset(x, y)
                    )
                } else {
                    // Multiple points - draw line and dots
                    val path = Path()
                    var firstPoint = true
                    
                    sortedPoints.forEachIndexed { index, point ->
                        val daysFromStart = ChronoUnit.DAYS.between(minDate, point.date)
                        val x = (daysFromStart.toFloat() / totalDays) * chartWidth
                        val y = paddingTop + usableHeight * (3 - point.value) / 2
                        
                        if (firstPoint) {
                            path.moveTo(x, y)
                            firstPoint = false
                        } else {
                            path.lineTo(x, y)
                        }
                    }
                    
                    // Draw the line
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                    
                    // Draw dots on top
                    sortedPoints.forEach { point ->
                        val daysFromStart = ChronoUnit.DAYS.between(minDate, point.date)
                        val x = (daysFromStart.toFloat() / totalDays) * chartWidth
                        val y = paddingTop + usableHeight * (3 - point.value) / 2
                        val color = SentimentColors.getColorForLevel(point.value)
                        
                        // White border
                        drawCircle(
                            color = Color.White,
                            radius = 6.dp.toPx(),
                            center = Offset(x, y)
                        )
                        // Colored fill
                        drawCircle(
                            color = color,
                            radius = 5.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }
        
        // X-axis labels (months)
        if (monthLabels.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                monthLabels.forEach { date ->
                    Text(
                        text = date.format(dateFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTrendChart(
    title: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No data yet",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

