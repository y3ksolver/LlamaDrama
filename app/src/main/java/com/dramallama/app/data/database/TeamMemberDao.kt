package com.dramallama.app.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamMemberDao {
    
    @Query("SELECT * FROM team_members ORDER BY CASE WHEN lastContactEpochDay IS NULL THEN 0 ELSE 1 END, lastContactEpochDay ASC")
    fun getAllMembersFlow(): Flow<List<TeamMember>>
    
    @Query("SELECT * FROM team_members ORDER BY CASE WHEN lastContactEpochDay IS NULL THEN 0 ELSE 1 END, lastContactEpochDay ASC")
    suspend fun getAllMembers(): List<TeamMember>
    
    @Query("SELECT * FROM team_members WHERE id = :id")
    fun getMemberByIdFlow(id: Long): Flow<TeamMember?>
    
    @Query("SELECT * FROM team_members WHERE id = :id")
    suspend fun getMemberById(id: Long): TeamMember?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: TeamMember): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<TeamMember>)
    
    @Update
    suspend fun update(member: TeamMember)
    
    @Delete
    suspend fun delete(member: TeamMember)
    
    @Query("DELETE FROM team_members")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM team_members")
    suspend fun getCount(): Int
}

