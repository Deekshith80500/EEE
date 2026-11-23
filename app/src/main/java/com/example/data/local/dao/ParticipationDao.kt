package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ParticipationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParticipationDao {
  @Query("SELECT * FROM participations WHERE activityId = :activityId")
  fun getParticipationsByActivity(activityId: String): Flow<List<ParticipationEntity>>

  @Query("SELECT * FROM participations WHERE activityId = :activityId")
  suspend fun getParticipationsByActivityOnce(activityId: String): List<ParticipationEntity>

  @Query("SELECT * FROM participations WHERE sourceType != 'DEMO'")
  fun getAllRealParticipations(): Flow<List<ParticipationEntity>>

  @Query("SELECT * FROM participations WHERE sourceType = 'DEMO'")
  fun getAllDemoParticipations(): Flow<List<ParticipationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertParticipation(participation: ParticipationEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertParticipations(participations: List<ParticipationEntity>)

  @Update
  suspend fun updateParticipation(participation: ParticipationEntity)

  @Query("DELETE FROM participations WHERE id = :id")
  suspend fun deleteParticipation(id: String)

  @Query("DELETE FROM participations WHERE sourceType = 'DEMO'")
  suspend fun deleteDemoParticipations()

  @Query("SELECT COUNT(*) FROM participations WHERE sourceType != 'DEMO'")
  fun getRealParticipationsCount(): Flow<Int>
}
