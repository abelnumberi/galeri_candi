package com.abelnumberi.galericandi.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TempleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemple(temple: Temple)

    @Update
    suspend fun updateTemple(temple: Temple)

    @Delete
    suspend fun deleteTemple(temple: Temple)

    @Query("SELECT * FROM temples WHERE userId = :userId")
    fun getAllTemples(userId: String): Flow<List<Temple>>

    @Query("SELECT * FROM temples WHERE userId = :userId")
    suspend fun getAllTemplesList(userId: String): List<Temple>

    @Query("DELETE FROM temples")
    suspend fun deleteAll()
}
