package com.dramallama.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "team_members")
data class TeamMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val lastContactEpochDay: Long? = null,  // Days since epoch for LocalDate
    val lastTopic: String? = null
)

