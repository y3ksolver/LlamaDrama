package com.dramallama.app.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "meeting_notes",
    foreignKeys = [
        ForeignKey(
            entity = TeamMember::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("memberId")]
)
data class MeetingNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long,
    val timestampEpochSecond: Long,  // Seconds since epoch for LocalDateTime
    val content: String,  // Meeting notes content
    // Sentiment tracking (optional)
    val mood: Int? = null,           // 1-5 scale (😞 to 😊)
    val productivity: Int? = null,   // 1-5 scale (Low to High)
    val flightRisk: Int? = null      // 1-4 scale (Low/Med/High/Critical)
)

