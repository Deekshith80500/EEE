package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ParticipantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParticipantDao {
  @Query("SELECT * FROM participants WHERE sourceType != 'DEMO' ORDER BY displayName ASC")
  fun getRealParticipants(): Flow<List<ParticipantEntity>>

  @Query("SELECT * FROM participants WHERE sourceType = 'DEMO' ORDER BY displayName ASC")
  fun getDemoParticipants(): Flow<List<ParticipantEntity>>

  @Query("SELECT * FROM participants ORDER BY displayName ASC")
  fun getAllParticipants(): Flow<List<ParticipantEntity>>

  @Query("SELECT * FROM participants WHERE id = :id LIMIT 1")
  fun getParticipantById(id: String): Flow<ParticipantEntity?>

  @Query("SELECT * FROM participants WHERE id = :id LIMIT 1")
  suspend fun getParticipantByIdOnce(id: String): ParticipantEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertParticipant(participant: ParticipantEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertParticipants(participants: List<ParticipantEntity>)

  @Update
  suspend fun updateParticipant(participant: ParticipantEntity)

  @Query("DELETE FROM participants WHERE id = :id")
  suspend fun deleteParticipantById(id: String)

  @Query("DELETE FROM participants WHERE sourceType = 'DEMO'")
  suspend fun deleteDemoParticipants()

  @Query("SELECT COUNT(*) FROM participants WHERE sourceType != 'DEMO'")
  fun getRealParticipantsCount(): Flow<Int>
}
