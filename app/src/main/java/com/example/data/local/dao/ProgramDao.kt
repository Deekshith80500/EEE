package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ProgramEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {
  @Query("SELECT * FROM programs WHERE id = :id LIMIT 1")
  fun getProgramById(id: String): Flow<ProgramEntity?>

  @Query("SELECT * FROM programs LIMIT 1")
  fun getCurrentProgram(): Flow<ProgramEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProgram(program: ProgramEntity)

  @Update
  suspend fun updateProgram(program: ProgramEntity)

  @Query("DELETE FROM programs WHERE isDemo = 1")
  suspend fun deleteDemoPrograms()
}
