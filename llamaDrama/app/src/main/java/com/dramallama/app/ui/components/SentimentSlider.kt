package com.dramallama.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Sentiment color schemes (3 levels)
object SentimentColors {
    val levelLow = Color(0xFFFF8A80)     // Red/Coral - Level 1
    val levelOkay = Color(0xFFFFB300)    // Amber - Level 2
    val levelGood = Color(0xFF4CAF50)    // Green - Level 3
    
    val flightRisk = Color(0xFFFF5252)   // Red for flight risk
    
    fun getColorForLevel(level: Int): Color {
        return when (level) {
            1 -> levelLow
            2 -> levelOkay
            3 -> levelGood
            else -> levelOkay
        }
    }
    
    fun getLabelForMood(level: Int): String {
        return when (level) {
            1 -> "Low"
            2 -> "Okay"
            3 -> "Good"
            else -> "—"
        }
    }
    
    fun getLabelForProductivity(level: Int): String {
        return when (level) {
            1 -> "Low"
            2 -> "Okay"
            3 -> "Good"
            else -> "—"
        }
    }
    
    fun getEmojiForMood(level: Int): String {
        return when (level) {
            1 -> "😞"
            2 -> "😐"
            3 -> "😊"
            else -> "😐"
        }
    }
    
    fun getEmojiForProductivity(level: Int): String {
        return when (level) {
            1 -> "📉"
            2 -> "📊"
            3 -> "📈"
            else -> "📊"
        }
    }
}

@Composable
fun SentimentSlider(
    label: String,
    emoji: String,
    value: Int?,
    onValueChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val levelLabels = listOf("Low", "Okay", "Good")
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(12.dp)
    ) {
        // Header row with emoji and label
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "  $label",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Level selector row with labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            levelLabels.forEachIndexed { index, levelLabel ->
                val level = index + 1
                val isSelected = value == level
                val color = SentimentColors.getColorForLevel(level)
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onValueChange(if (value == level) null else level) }
                        .background(
                            if (isSelected) color.copy(alpha = 0.2f) 
                            else Color.Transparent
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) color 
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = levelLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun FlightRiskCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable { onCheckedChange(!checked) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🚨", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "  Flight Risk",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = SentimentColors.flightRisk,
                checkmarkColor = Color.White
            )
        )
        
        if (checked) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "At Risk",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = SentimentColors.flightRisk
            )
        }
    }
}

