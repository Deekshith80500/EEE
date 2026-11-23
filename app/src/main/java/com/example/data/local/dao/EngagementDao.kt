package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.EngagementDefinitionEntity
import com.example.data.local.entity.EngagementRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EngagementDao {
  @Query("SELECT * FROM engagement_definitions WHERE active = 1")
  fun getActiveDefinitions(): Flow<List<EngagementDefinitionEntity>>

  @Query("SELECT * FROM engagement_definitions WHERE id = :id LIMIT 1")
  fun getDefinitionById(id: String): Flow<EngagementDefinitionEntity?>

  @Query("SELECT * FROM engagement_definitions")
  fun getAllDefinitions(): Flow<List<EngagementDefinitionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDefinition(definition: EngagementDefinitionEntity)

  @Update
  suspend fun updateDefinition(definition: EngagementDefinitionEntity)

  @Query("SELECT * FROM engagement_records WHERE activityId = :activityId")
  fun getRecordsByActivity(activityId: String): Flow<List<EngagementRecordEntity>>

  @Query("SELECT * FROM engagement_records WHERE sourceType != 'DEMO'")
  fun getAllRealRecords(): Flow<List<EngagementRecordEntity>>

  @Query("SELECT * FROM engagement_records WHERE sourceType = 'DEMO'")
  fun getAllDemoRecords(): Flow<List<EngagementRecordEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRecord(record: EngagementRecordEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRecords(records: List<EngagementRecordEntity>)

  @Query("DELETE FROM engagement_records WHERE id = :id")
  suspend fun deleteRecord(id: String)

  @Query("DELETE FROM engagement_records WHERE sourceType = 'DEMO'")
  suspend fun deleteDemoRecords()

  @Query("DELETE FROM engagement_definitions WHERE id LIKE 'demo_%'")
  suspend fun deleteDemoDefinitions()
}
