package com.abelnumberi.galericandi.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Temple::class], version = 1, exportSchema = false)
abstract class TempleDatabase : RoomDatabase() {
    abstract fun templeDao(): TempleDao

    companion object {
        @Volatile
        private var INSTANCE: TempleDatabase? = null

        fun getDatabase(context: Context): TempleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TempleDatabase::class.java,
                    "temple_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
