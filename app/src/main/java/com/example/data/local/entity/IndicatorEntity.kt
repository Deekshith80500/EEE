package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "indicators",
  indices = [
    Index(value = ["programId"])
  ]
)
data class IndicatorEntity(
  @PrimaryKey val id: String,
  val programId: String,
  val name: String, // e.g. "Financial Literacy Awareness", "Peer Collaboration"
  val category: String = "SKILL", // SKILL or AWARENESS
  val definition: String = "",
  val unit: String = "points",
  val measurementMethod: String = "Standardized Assessment",
  val createdAt: Long = System.currentTimeMillis()
)
