package com.meaning.app.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Database(
    entities = [QuantizedNarrativeEntity::class, NarrativeConnectionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NarrativeDatabase : RoomDatabase() {
    abstract fun dao(): NarrativeDao
    
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
                        CoroutineScope(Dispatchers.IO).launch {
                            populateInitialData(getInstance(context))
                        }
                    }
                })
                .addMigrations() // Jövőbeli migrációk
                .setQueryExecutor(Dispatchers.IO.asExecutor())
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        private suspend fun populateInitialData(database: NarrativeDatabase) {
            val dao = database.dao()
            
            // Alap metaforák létrehozása
            val basicMetaphors = listOf(
                Triple("tenger", "természet", floatArrayOf(0.12f, 0.88f, -0.45f, 0.67f)),
                Triple("szabadság", "érzelem", floatArrayOf(0.15f, 0.85f, -0.40f, 0.70f)),
                Triple("hegy", "természet", floatArrayOf(0.05f, 0.60f, 0.30f, -0.45f)),
                Triple("szeretet", "érzelem", floatArrayOf(0.90f, 0.10f, -0.30f, 0.80f)),
                Triple("tűz", "természet", floatArrayOf(0.70f, -0.20f, 0.85f, 0.10f)),
                Triple("félelem", "érzelem", floatArrayOf(-0.65f, 0.40f, 0.25f, -0.70f))
            )
            
            basicMetaphors.forEachIndexed { index, (term, family, vector) ->
                val entity = QuantizedNarrativeEntity(
                    term = term,
                    metaphorFamily = family,
                    semanticDensity = 0.8f + (Math.random() * 0.2).toFloat(),
                    coordX = (Math.random() * 2 - 1).toFloat(),
                    coordY = (Math.random() * 2 - 1).toFloat(),
                    coordZ = (Math.random() * 2 - 1).toFloat(),
                    vectorInt8 = QuantizationEngine.quantizeToINT8(vector),
                    vectorFP32 = vector.toByteArray(),
                    attentionWeight = 1.0f
                )
                dao.insert(entity)
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromDate(date: Date?): Long? = date?.time
    
    @TypeConverter
    fun toDate(timestamp: Long?): Date? = timestamp?.let { Date(it) }
    
    @TypeConverter
    fun fromFloatArray(array: FloatArray?): String? {
        return array?.joinToString(",") { it.toString() }
    }
    
    @TypeConverter
    fun toFloatArray(data: String?): FloatArray? {
        return data?.split(",")?.map { it.toFloat() }?.toFloatArray()
    }
}

// Extensions
fun FloatArray.toByteArray(): ByteArray {
    val buffer = java.nio.ByteBuffer.allocate(this.size * 4)
    buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN)
    this.forEach { buffer.putFloat(it) }
    return buffer.array()
}

fun ByteArray.toFloatArray(): FloatArray {
    val buffer = java.nio.ByteBuffer.wrap(this)
    buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN)
    val result = FloatArray(this.size / 4)
    for (i in result.indices) {
        result[i] = buffer.float
    }
    return result
}
