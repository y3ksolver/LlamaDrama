package com.example.llamadrama.data.database

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
    val content: String  // Meeting notes content
)

