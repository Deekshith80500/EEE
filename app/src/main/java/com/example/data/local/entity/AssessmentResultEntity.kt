package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "assessment_results",
  indices = [
    Index(value = ["assessmentId"]),
    Index(value = ["participantId"]),
    Index(value = ["assessmentId", "participantId"], unique = true)
  ]
)
data class AssessmentResultEntity(
  @PrimaryKey val id: String,
  val assessmentId: String,
  val participantId: String,
  val rawScore: Double,
  val recordedAt: Long = System.currentTimeMillis(),
  val recordedBy: String = "Staff",
  val sourceType: String = "USER_ENTERED" // USER_ENTERED, DEMO
)
