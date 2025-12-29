package com.meaning.app.db
import android.content.Context
import androidx.room.*
@Database(entities = [NarrativeEntity::class], version = 1)
abstract class NarrativeDatabase : RoomDatabase() {
    abstract fun narrativeDao(): NarrativeDao
    companion object {
        @Volatile private var INSTANCE: NarrativeDatabase? = null
        fun getDatabase(context: Context): NarrativeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, NarrativeDatabase::class.java, "meaning_db").build()
                INSTANCE = instance
                instance
            }
        }
    }
}
