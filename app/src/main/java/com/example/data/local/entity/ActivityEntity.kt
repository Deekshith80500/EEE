package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "activities",
  indices = [
    Index(value = ["programId"]),
    Index(value = ["status"]),
    Index(value = ["scheduledAt"])
  ]
)
data class ActivityEntity(
  @PrimaryKey val id: String,
  val programId: String,
  val title: String,
  val description: String = "",
  val scheduledAt: Long = System.currentTimeMillis(),
  val durationMinutes: Int = 60,
  val status: String = "SCHEDULED", // DRAFT, SCHEDULED, ACTIVE, COMPLETED
  val mentorName: String = "",
  val locationOrMedium: String = "",
  val sourceType: String = "USER_ENTERED", // USER_ENTERED, IMPORTED, DEMO
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)
