package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "participants",
  indices = [
    Index(value = ["programId"]),
    Index(value = ["externalReference"])
  ]
)
data class ParticipantEntity(
  @PrimaryKey val id: String,
  val programId: String,
  val displayName: String,
  val externalReference: String? = null,
  val groupCohort: String? = null,
  val active: Boolean = true,
  val sourceType: String = "USER_ENTERED", // USER_ENTERED, DEMO
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)
