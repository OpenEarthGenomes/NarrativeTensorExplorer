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
                    "narrative_space.db"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Első indításkor látványos adatok generálása
                        CoroutineScope(Dispatchers.IO).launch {
                            populateInitialData(getInstance(context))
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        private suspend fun populateInitialData(database: NarrativeDatabase) {
            val dao = database.narrativeDao()
            if (dao.getCount() > 0) return

            val basicMetaphors = listOf(
                Triple("tenger", "természet", floatArrayOf(0.1f, 0.8f, -0.4f)),
                Triple("szabadság", "érzelem", floatArrayOf(0.5f, 0.2f, 0.9f)),
                Triple("hegy", "természet", floatArrayOf(-0.3f, 0.7f, 0.1f)),
                Triple("idő", "absztrakt", floatArrayOf(0.0f, 0.0f, 0.8f))
            )
            
            basicMetaphors.forEach { (term, family, vector) ->
                dao.insert(QuantizedNarrativeEntity(
                    term = term,
                    metaphorFamily = family,
                    semanticDensity = 0.85f,
                    coordX = vector[0],
                    coordY = vector[1],
                    coordZ = vector[2],
                    vectorInt8 = QuantizationEngine.quantizeToINT8(vector),
                    vectorFP32 = null
                ))
            }
        }
    }
}
