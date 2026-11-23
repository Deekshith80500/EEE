package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
  @Query("SELECT * FROM activities WHERE sourceType != 'DEMO' ORDER BY scheduledAt DESC")
  fun getRealActivities(): Flow<List<ActivityEntity>>

  @Query("SELECT * FROM activities WHERE sourceType = 'DEMO' ORDER BY scheduledAt DESC")
  fun getDemoActivities(): Flow<List<ActivityEntity>>

  @Query("SELECT * FROM activities ORDER BY scheduledAt DESC")
  fun getAllActivities(): Flow<List<ActivityEntity>>

  @Query("SELECT * FROM activities WHERE id = :id LIMIT 1")
  fun getActivityById(id: String): Flow<ActivityEntity?>

  @Query("SELECT * FROM activities WHERE id = :id LIMIT 1")
  suspend fun getActivityByIdOnce(id: String): ActivityEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertActivity(activity: ActivityEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertActivities(activities: List<ActivityEntity>)

  @Update
  suspend fun updateActivity(activity: ActivityEntity)

  @Query("DELETE FROM activities WHERE id = :id")
  suspend fun deleteActivityById(id: String)

  @Query("DELETE FROM activities WHERE sourceType = 'DEMO'")
  suspend fun deleteDemoActivities()

  @Query("SELECT COUNT(*) FROM activities WHERE sourceType != 'DEMO'")
  fun getRealActivitiesCount(): Flow<Int>
}
