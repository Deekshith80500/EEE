package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ActivityDao
import com.example.data.local.dao.AssessmentDao
import com.example.data.local.dao.EngagementDao
import com.example.data.local.dao.ParticipantDao
import com.example.data.local.dao.ParticipationDao
import com.example.data.local.dao.ProgramDao
import com.example.data.local.entity.ActivityEntity
import com.example.data.local.entity.AssessmentEntity
import com.example.data.local.entity.AssessmentResultEntity
import com.example.data.local.entity.EngagementDefinitionEntity
import com.example.data.local.entity.EngagementRecordEntity
import com.example.data.local.entity.IndicatorEntity
import com.example.data.local.entity.ParticipantEntity
import com.example.data.local.entity.ParticipationEntity
import com.example.data.local.entity.ProgramEntity
import com.example.data.local.entity.SyncQueueEntity

@Database(
  entities = [
    ProgramEntity::class,
    ActivityEntity::class,
    ParticipantEntity::class,
    ParticipationEntity::class,
    EngagementDefinitionEntity::class,
    EngagementRecordEntity::class,
    IndicatorEntity::class,
    AssessmentEntity::class,
    AssessmentResultEntity::class,
    SyncQueueEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun programDao(): ProgramDao
  abstract fun activityDao(): ActivityDao
  abstract fun participantDao(): ParticipantDao
  abstract fun participationDao(): ParticipationDao
  abstract fun engagementDao(): EngagementDao
  abstract fun assessmentDao(): AssessmentDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "youth_monitor_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
