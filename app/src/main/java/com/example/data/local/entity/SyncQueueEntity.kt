package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
  @PrimaryKey val id: String,
  val entityType: String,
  val entityId: String,
  val operation: String, // CREATE, UPDATE, DELETE
  val payload: String = "",
  val createdAt: Long = System.currentTimeMillis(),
  val attemptCount: Int = 0,
  val lastAttemptAt: Long? = null,
  val status: String = "LOCAL_ONLY" // LOCAL_ONLY, PENDING, SYNCED, FAILED
)
