package com.dramallama.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dramallama.app.data.database.MeetingNote
import com.dramallama.app.data.database.TeamMember
import com.dramallama.app.data.repository.toLocalDate
import com.dramallama.app.data.repository.toLocalDateTime
import com.dramallama.app.ui.components.FlightRiskCheckbox
import com.dramallama.app.ui.components.SentimentSlider
import com.dramallama.app.ui.components.TrendChart
import com.dramallama.app.ui.components.TrendDataPoint
import com.dramallama.app.ui.viewmodel.MemberDetailViewModel
import com.dramallama.app.ui.viewmodel.MemberDetailViewModel.CadenceData
import com.dramallama.app.ui.viewmodel.MemberDetailViewModel.FrequencyTrend
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Pre-defined formatters to avoid creating on each recomposition
private val headerDateFormatter = DateTimeFormatter.ofPattern("MMM d")
private val noteDateFormatter = DateTimeFormatter.ofPattern("MMM d, HH:mm")
private val pickerDateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
private val pickerTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    viewModel: MemberDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val member by viewModel.member.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val showNoteSheet by viewModel.showNoteSheet.collectAsState()
    val editingNote by viewModel.editingNote.collectAsState()
    val noteContent by viewModel.noteContent.collectAsState()
    val noteDate by viewModel.noteDate.collectAsState()
    val noteTime by viewModel.noteTime.collectAsState()
    val noteMood by viewModel.noteMood.collectAsState()
    val noteProductivity by viewModel.noteProductivity.collectAsState()
    val noteFlightRisk by viewModel.noteFlightRisk.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Sentiment trend data
    val moodTrend by viewModel.moodTrend.collectAsState()
    val productivityTrend by viewModel.productivityTrend.collectAsState()
    val totalMeetings by viewModel.totalMeetings.collectAsState()
    val cadenceData by viewModel.cadenceData.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(member?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddNoteSheet() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add meeting note")
            }
        }
    ) { padding ->
        member?.let { currentMember ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    CompactMemberHeader(member = currentMember)
                }
                
                // Meeting Cadence Section
                item {
                    MeetingCadenceCard(
                        cadenceData = cadenceData,
                        totalMeetings = totalMeetings
                    )
                }
                
                // Sentiment Trends Section (collapsible)
                item {
                    SentimentTrendsCard(
                        moodTrend = moodTrend,
                        productivityTrend = productivityTrend,
                        totalMeetings = totalMeetings
                    )
                }
                
                if (notes.isNotEmpty()) {
                    items(notes, key = { it.id }) { note ->
                        SwipeToDeleteNoteCard(
                            note = note,
                            onEdit = { viewModel.showEditNoteSheet(note) },
                            onDelete = { viewModel.deleteNote(note) }
                        )
                    }
                } else {
                    item {
                        EmptyNotesPlaceholder()
                    }
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        } ?: run {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading...")
            }
        }
    }
    
    if (showNoteSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.hideNoteSheet() },
            sheetState = sheetState
        ) {
            NoteSheet(
                isEditing = editingNote != null,
                noteContent = noteContent,
                onNoteContentChange = { viewModel.updateNoteContent(it) },
                noteDate = noteDate,
                onDateChange = { viewModel.updateNoteDate(it) },
                noteTime = noteTime,
                onTimeChange = { viewModel.updateNoteTime(it) },
                mood = noteMood,
                onMoodChange = { viewModel.updateNoteMood(it) },
                productivity = noteProductivity,
                onProductivityChange = { viewModel.updateNoteProductivity(it) },
                flightRisk = noteFlightRisk,
                onFlightRiskChange = { viewModel.updateNoteFlightRisk(it) },
                onSave = { viewModel.saveNote() },
                onCancel = { viewModel.hideNoteSheet() }
            )
        }
    }
}

@Composable
fun CompactMemberHeader(member: TeamMember) {
    val today = remember { LocalDate.now() }
    val lastContact = remember(member.lastContactEpochDay) { 
        member.lastContactEpochDay?.toLocalDate() 
    }
    val daysSince = remember(lastContact, today) { 
        lastContact?.let { ChronoUnit.DAYS.between(it, today).toInt() } 
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Compact avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.name.take(2).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            // Inline stats
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (lastContact != null) {
                Text(
                        text = lastContact.format(headerDateFormatter),
                        style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${daysSince}d ago",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (daysSince != null && daysSince >= 14) 
                            MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
        Text(
                        text = "No meetings yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteNoteCard(
    note: MeetingNote,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showDeleteConfirmation = true
                false // Don't dismiss yet, wait for confirmation
            } else false
        }
    )
    
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            // Simple red background without trash icon
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.error)
            )
        },
        enableDismissFromStartToEnd = false
    ) {
        MeetingNoteCard(note = note, onClick = onEdit)
    }
    
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Meeting Note") },
            text = { Text("Are you sure you want to delete this meeting note?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MeetingNoteCard(
    note: MeetingNote,
    onClick: () -> Unit = {}
) {
    val timestamp = remember(note.timestampEpochSecond) { 
        note.timestampEpochSecond.toLocalDateTime() 
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            // Fully opaque so swipe looks clean
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Date header
            Text(
                text = timestamp.format(noteDateFormatter),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Flowing text content (cleaned up)
            val cleanedContent = remember(note.content) {
                note.content.lines()
                    .filter { it.isNotBlank() }
                    .joinToString("\n") { line ->
                        val cleaned = line.trimStart('-', '*', '•', ' ', '\t')
                        "• $cleaned"
                    }
            }
            
                        Text(
                text = cleanedContent,
                            style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
            )
            
            // Sentiment indicators below content (bigger with labels)
            SentimentIndicatorsRow(
                mood = note.mood,
                productivity = note.productivity,
                flightRisk = note.flightRisk
            )
        }
    }
}

@Composable
fun SentimentIndicatorsRow(
    mood: Int?,
    productivity: Int?,
    flightRisk: Int?,
    modifier: Modifier = Modifier
) {
    val isAtRisk = flightRisk != null && flightRisk > 0
    val hasSentiment = mood != null || productivity != null || isAtRisk
    
    if (hasSentiment) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            mood?.let {
                SentimentChip(
                    emoji = when (it) { 1 -> "😞"; 2 -> "😐"; else -> "😊" },
                    label = when (it) { 1 -> "Low"; 2 -> "Okay"; else -> "Good" },
                    category = "Mood"
                )
            }
            productivity?.let {
                SentimentChip(
                    emoji = when (it) { 1 -> "📉"; 2 -> "📊"; else -> "📈" },
                    label = when (it) { 1 -> "Low"; 2 -> "Okay"; else -> "Good" },
                    category = "Productivity"
                )
            }
            if (isAtRisk) {
                SentimentChip(
                    emoji = "⚠️",
                    label = "At Risk",
                    category = "Flight",
                    isWarning = true
                )
            }
        }
    }
}

@Composable
fun SentimentChip(
    emoji: String,
    label: String,
    category: String,
    isWarning: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isWarning) 
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
    else 
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    
    val textColor = if (isWarning)
        MaterialTheme.colorScheme.error
    else
        MaterialTheme.colorScheme.onSurface
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
                        Text(
            text = emoji,
            style = MaterialTheme.typography.titleMedium
        )
        Column {
            Text(
                text = category,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@Composable
fun EmptyNotesPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "📝",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "No notes yet · Tap + to add",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteSheet(
    isEditing: Boolean,
    noteContent: String,
    onNoteContentChange: (String) -> Unit,
    noteDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    noteTime: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    mood: Int?,
    onMoodChange: (Int?) -> Unit,
    productivity: Int?,
    onProductivityChange: (Int?) -> Unit,
    flightRisk: Int?,
    onFlightRiskChange: (Int?) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .imePadding()
    ) {
        item {
        Text(
            if (isEditing) "Edit Meeting Note" else "Add Meeting Note",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Date and Time selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Date picker button
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showDatePicker = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📅", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "Date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            noteDate.format(pickerDateFormatter),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Time picker button
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showTimePicker = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🕐", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "Time",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            noteTime.format(pickerTimeFormatter),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
            Spacer(modifier = Modifier.height(20.dp))
            
            // Sentiment Tracking Section
            Text(
                "Sentiment Tracking (optional)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Mood Slider (3 levels)
            SentimentSlider(
                label = "Mood",
                emoji = "😊",
                value = mood,
                onValueChange = onMoodChange
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Productivity Slider (3 levels)
            SentimentSlider(
                label = "Productivity",
                emoji = "📈",
                value = productivity,
                onValueChange = onProductivityChange
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Flight Risk Checkbox
            FlightRiskCheckbox(
                checked = flightRisk != null && flightRisk > 0,
                onCheckedChange = { checked -> 
                    onFlightRiskChange(if (checked) 1 else null) 
                }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            "Enter your 1-on-1 notes (one point per line)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = noteContent,
            onValueChange = onNoteContentChange,
            modifier = Modifier
                .fillMaxWidth()
                    .height(150.dp),
            placeholder = { 
                Text("- Discussion point 1\n- Discussion point 2\n- Action items...")
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = onSave,
                enabled = noteContent.isNotBlank()
            ) {
                Text(if (isEditing) "Update" else "Save")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        }
    }
    
    // Date Picker Dialog
    // Note: Material3 DatePicker works with UTC millis internally
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = noteDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            onDateChange(selectedDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false  // Hide the pencil icon
            )
        }
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = noteTime.hour,
            initialMinute = noteTime.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeChange(LocalTime.of(timePickerState.hour, timePickerState.minute))
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MeetingCadenceCard(
    cadenceData: CadenceData,
    totalMeetings: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📅", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Meeting Cadence",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (totalMeetings < 2) {
                Text(
                    text = "Need at least 2 meetings to calculate cadence",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            } else {
                cadenceData.avgFrequencyDays?.let { avgFreq ->
                    // Average frequency with edge case handling
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayText = when {
                            avgFreq < 1.0 -> "Same day"
                            avgFreq == 1.0 -> "Avg 1 day"
                            else -> "Avg ${avgFreq.toInt()} days"
                        }
                        
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Cadence rating badge
                        val cadenceLabel = if (cadenceData.isRegular) "Regular" else "Irregular"
                        val cadenceColor = if (cadenceData.isRegular) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(cadenceColor.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = cadenceLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = cadenceColor
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Trend indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val trendIcon = when (cadenceData.trend) {
                            FrequencyTrend.MORE_FREQUENT -> "↑"
                            FrequencyTrend.LESS_FREQUENT -> "↓"
                            FrequencyTrend.STABLE -> "→"
                        }
                        val trendText = when (cadenceData.trend) {
                            FrequencyTrend.MORE_FREQUENT -> "More frequent"
                            FrequencyTrend.LESS_FREQUENT -> "Less frequent"
                            FrequencyTrend.STABLE -> "Stable"
                        }
                        val trendColor = when (cadenceData.trend) {
                            FrequencyTrend.MORE_FREQUENT -> MaterialTheme.colorScheme.primary
                            FrequencyTrend.LESS_FREQUENT -> MaterialTheme.colorScheme.error
                            FrequencyTrend.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        
                        Text(
                            text = trendIcon,
                            style = MaterialTheme.typography.bodyMedium,
                            color = trendColor
                        )
                        Text(
                            text = trendText,
                            style = MaterialTheme.typography.bodySmall,
                            color = trendColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SentimentTrendsCard(
    moodTrend: List<TrendDataPoint>,
    productivityTrend: List<TrendDataPoint>,
    totalMeetings: Int,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val hasTrendData = moodTrend.isNotEmpty() || productivityTrend.isNotEmpty()
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Compact header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📊", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Trends",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (hasTrendData) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "·",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "${moodTrend.size + productivityTrend.size} data points",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp 
                        else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            // Expandable content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                if (hasTrendData) {
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Mood trend chart
                        TrendChart(
                            title = "Mood",
                            emoji = "😊",
                            dataPoints = moodTrend
                        )
                        
                        // Productivity trend chart
                        TrendChart(
                            title = "Productivity",
                            emoji = "📈",
                            dataPoints = productivityTrend
                        )
                    }
                } else {
                    // Compact no-data message
                    Text(
                        text = "Add mood/productivity to notes to see trends",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

