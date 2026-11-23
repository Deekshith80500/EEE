package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "engagement_records",
  indices = [
    Index(value = ["definitionId"]),
    Index(value = ["activityId"]),
    Index(value = ["participantId"]),
    Index(value = ["activityId", "participantId", "definitionId"], unique = true)
  ]
)
data class EngagementRecordEntity(
  @PrimaryKey val id: String,
  val definitionId: String,
  val activityId: String,
  val participantId: String,
  val score: Double,
  val notes: String? = null,
  val recordedAt: Long = System.currentTimeMillis(),
  val recordedBy: String = "Staff",
  val sourceType: String = "USER_ENTERED" // USER_ENTERED, DEMO
)
