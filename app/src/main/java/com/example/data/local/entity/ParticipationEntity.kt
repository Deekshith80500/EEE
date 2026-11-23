package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "participations",
  indices = [
    Index(value = ["activityId", "participantId"], unique = true),
    Index(value = ["activityId"]),
    Index(value = ["participantId"]),
    Index(value = ["status"])
  ]
)
data class ParticipationEntity(
  @PrimaryKey val id: String,
  val activityId: String,
  val participantId: String,
  val status: String, // PARTICIPATED, DID_NOT_PARTICIPATE, NOT_RECORDED
  val recordedAt: Long = System.currentTimeMillis(),
  val recordedBy: String = "Staff",
  val sourceReference: String? = null,
  val sourceType: String = "USER_ENTERED" // USER_ENTERED, DEMO, SYNCED
)
