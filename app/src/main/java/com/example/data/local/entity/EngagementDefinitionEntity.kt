package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "engagement_definitions",
  indices = [
    Index(value = ["programId"]),
    Index(value = ["active"])
  ]
)
data class EngagementDefinitionEntity(
  @PrimaryKey val id: String,
  val programId: String,
  val name: String, // e.g. "Structured Session Engagement"
  val methodology: String, // e.g. "RATING_SCALE" (1-5), "INTERACTION_COUNT"
  val description: String = "",
  val scaleMin: Double = 1.0,
  val scaleMax: Double = 5.0,
  val unit: String = "scale (1-5)",
  val version: String = "1.0",
  val active: Boolean = true,
  val createdAt: Long = System.currentTimeMillis()
)
