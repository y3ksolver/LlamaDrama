package com.dramallama.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MoodBadge(
    value: Int,
    modifier: Modifier = Modifier
) {
    val color = SentimentColors.getColorForLevel(value)
    val label = SentimentColors.getLabelForMood(value)
    val emoji = SentimentColors.getEmojiForMood(value)
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun ProductivityBadge(
    value: Int,
    modifier: Modifier = Modifier
) {
    val color = SentimentColors.getColorForLevel(value)
    val label = SentimentColors.getLabelForProductivity(value)
    val emoji = SentimentColors.getEmojiForProductivity(value)
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun FlightRiskBadge(
    modifier: Modifier = Modifier
) {
    val color = SentimentColors.flightRisk
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
            Text(
                text = "At Risk",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun SentimentBadgesRow(
    mood: Int?,
    productivity: Int?,
    flightRisk: Int?,
    modifier: Modifier = Modifier
) {
    // flightRisk: 1 means "at risk" (checked), null or 0 means not at risk
    val isAtRisk = flightRisk != null && flightRisk > 0
    
    // Only show row if at least one sentiment value is set
    if (mood != null || productivity != null || isAtRisk) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            mood?.let {
                MoodBadge(value = it)
            }
            productivity?.let {
                ProductivityBadge(value = it)
            }
            if (isAtRisk) {
                FlightRiskBadge()
            }
        }
    }
}

