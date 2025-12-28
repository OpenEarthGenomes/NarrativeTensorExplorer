package com.meaning.app.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.meaning.app.kernel.QuantizationEngine

@Database(
    entities = [QuantizedNarrativeEntity::class, NarrativeConnectionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NarrativeDatabase : RoomDatabase() {
    
    abstract fun narrativeDao(): NarrativeDao
    abstract fun connectionDao(): NarrativeConnectionDao
    
    companion object {
        @Volatile
        private var INSTANCE: NarrativeDatabase? = null
        
        fun getInstance(context: Context): NarrativeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NarrativeDatabase::class.java,
                    "narrative_space_v1.db"
                )
                .fallbackToDestructiveMigration() // Fejlesztés alatt törli és újrahúzza, ha változik a séma
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = database.narrativeDao()
                    // Kezdő mag-adatok betöltése a rendszerbe
                    val initialSeeds = listOf(
                        Triple("Tudat", "kernel", floatArrayOf(0.0f, 0.0f, 0.0f)),
                        Triple("Idő", "absztrakt", floatArrayOf(0.5f, -0.2f, 0.8f)),
                        Triple("Káosz", "rendszer", floatArrayOf(-0.7f, 0.4f, -0.3f))
                    )
                    
                    initialSeeds.forEach { (term, family, coords) ->
                        dao.insert(QuantizedNarrativeEntity(
                            term = term,
                            metaphorFamily = family,
                            semanticDensity = 0.9f,
                            coordX = coords[0],
                            coordY = coords[1],
                            coordZ = coords[2],
                            vectorInt8 = QuantizationEngine.quantizeToINT8(floatArrayOf(coords[0], coords[1], coords[2], 0.1f, 0.5f))
                        ))
                    }
                }
            }
        }
    }
}

