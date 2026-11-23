package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "assessments",
  indices = [
    Index(value = ["activityId"]),
    Index(value = ["indicatorId"]),
    Index(value = ["assessmentType"])
  ]
)
data class AssessmentEntity(
  @PrimaryKey val id: String,
  val activityId: String,
  val indicatorId: String,
  val title: String,
  val assessmentType: String, // PRE_ACTIVITY or POST_ACTIVITY
  val maximumScore: Double = 100.0,
  val measurementDefinition: String = "Raw Score Scale",
  val version: String = "1.0",
  val createdAt: Long = System.currentTimeMillis()
)
