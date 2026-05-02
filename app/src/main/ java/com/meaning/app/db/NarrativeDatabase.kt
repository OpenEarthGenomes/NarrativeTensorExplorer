package com.meaning.app.db

import android.content.Context
import androidx.room.*

@Database(
    entities = [
        NarrativeEntity::class, 
        QuantizedNarrativeEntity::class, 
        NarrativeConnectionEntity::class
    ], 
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class) // A Date kezeléséhez kell
abstract class NarrativeDatabase : RoomDatabase() {
    abstract fun narrativeDao(): NarrativeDao
    abstract fun connectionDao(): NarrativeConnectionDao
    // Itt adhatsz hozzá egy speciális DAO-t a kvantált adatokhoz ha kell

    companion object {
        @Volatile private var INSTANCE: NarrativeDatabase? = null
        fun getDatabase(context: Context): NarrativeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NarrativeDatabase::class.java,
                    "meaning_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
