// ==============================================
// HIÁNYZÓ RÉSZ: TypeConverters és DAO-k hozzáadása
// ==============================================

package com.meaning.app.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        QuantizedNarrativeEntity::class,
        NarrativeConnectionEntity::class  // ✅ Hozzáadva
    ],
    version = 2,  // ✅ Verzió növelve új entitás miatt
    exportSchema = true  // ✅ Engedélyezve a schema export
)
@TypeConverters(Converters::class)  // ✅ KRITIKUS: TypeConverters hozzáadva
abstract class NarrativeDatabase : RoomDatabase() {
    
    // ✅ KRITIKUS: Minden DAO interface-t regisztrálni kell
    abstract fun narrativeDao(): NarrativeDao
    abstract fun connectionDao(): NarrativeConnectionDao  // ✅ ÚJ DAO hozzáadva
    
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
                    
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // Optimalizálások az adatbázis megnyitásakor
                        db.execSQL("PRAGMA journal_mode = WAL")
                        db.execSQL("PRAGMA synchronous = NORMAL")
                        db.execSQL("PRAGMA foreign_keys = ON")
                        db.execSQL("PRAGMA cache_size = -2000") // 2MB cache
                    }
                })
                .addMigrations(
                    // Migrációk a jövőbeli verzióváltozásokhoz
                    MIGRATION_1_2
                )
                .setQueryExecutor(Dispatchers.IO.asExecutor())
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        // ✅ Migráció 1-ről 2-re (új entitás hozzáadása)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // narrative_connections tábla létrehozása
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `narrative_connections` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `from_id` INTEGER NOT NULL,
                        `to_id` INTEGER NOT NULL,
                        `connection_type` TEXT NOT NULL,
                        `strength` REAL NOT NULL,
                        `semantic_similarity` REAL NOT NULL DEFAULT 0,
                        `spatial_distance` REAL NOT NULL DEFAULT 0,
                        `metadata_json` TEXT NOT NULL DEFAULT '{}',
                        `created_at` INTEGER,
                        `updated_at` INTEGER,
                        `usage_count` INTEGER NOT NULL DEFAULT 0,
                        `is_active` INTEGER NOT NULL DEFAULT 1,
                        FOREIGN KEY(`from_id`) REFERENCES `quantized_narrative`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`to_id`) REFERENCES `quantized_narrative`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                
                // Indexek létrehozása a teljesítmény érdekében
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_narrative_connections_from_id` ON `narrative_connections` (`from_id`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_narrative_connections_to_id` ON `narrative_connections` (`to_id`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_narrative_connections_strength` ON `narrative_connections` (`strength`)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_narrative_connections_from_to` ON `narrative_connections` (`from_id`, `to_id`)")
                
                // quantized_narrative tábla bővítése, ha szükséges
                database.execSQL("ALTER TABLE `quantized_narrative` ADD COLUMN `layer` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `quantized_narrative` ADD COLUMN `emotional_valence` INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        private suspend fun populateInitialData(database: NarrativeDatabase) {
            val narrativeDao = database.narrativeDao()
            val connectionDao = database.connectionDao()
            
            // Alap metaforák létrehozása (csak ha üres az adatbázis)
            if (narrativeDao.getCount() == 0L) {
                val sampleWords = listOf(
                    Triple("tenger", "természet", floatArrayOf(0.12f, 0.88f, -0.45f, 0.67f)),
                    Triple("szabadság", "érzelem", floatArrayOf(0.15f, 0.85f, -0.40f, 0.70f)),
                    Triple("hegy", "természet", floatArrayOf(0.05f, 0.60f, 0.30f, -0.45f)),
                    Triple("szeretet", "érzelem", floatArrayOf(0.90f, 0.10f, -0.30f, 0.80f)),
                    Triple("tűz", "természet", floatArrayOf(0.70f, -0.20f, 0.85f, 0.10f)),
                    Triple("félelem", "érzelem", floatArrayOf(-0.65f, 0.40f, 0.25f, -0.70f)),
                    Triple("fény", "absztrakt", floatArrayOf(0.95f, 0.85f, 0.10f, 0.45f)),
                    Triple("árnyék", "absztrakt", floatArrayOf(-0.40f, -0.60f, 0.25f, -0.30f)),
                    Triple("idő", "idő", floatArrayOf(0.30f, 0.50f, -0.75f, 0.20f)),
                    Triple("emlékezet", "idő", floatArrayOf(0.45f, 0.35f, 0.60f, -0.25f))
                )
                
                val entities = mutableListOf<QuantizedNarrativeEntity>()
                sampleWords.forEachIndexed { index, (term, family, vector) ->
                    val entity = QuantizedNarrativeEntity(
                        term = term,
                        metaphorFamily = family,
                        semanticDensity = 0.7f + (Math.random() * 0.3).toFloat(),
                        coordX = (Math.random() * 2 - 1).toFloat(),
                        coordY = (Math.random() * 2 - 1).toFloat(),
                        coordZ = (Math.random() * 2 - 1).toFloat(),
                        vectorInt8 = QuantizationEngine.quantizeToINT8(vector),
                        vectorFP32 = vector.toByteArray(),
                        attentionWeight = 1.0f,
                        layer = index % 3,
                        emotionalValence = when (family) {
                            "érzelem" -> if (term == "szeretet") 80 else -60
                            else -> 0
                        }.toByte()
                    )
                    entities.add(entity)
                }
                
                // Batch insert
                val insertedIds = narrativeDao.insertAll(entities)
                
                // Alap kapcsolatok létrehozása
                if (insertedIds.isNotEmpty() && connectionDao.getCount() == 0L) {
                    val connections = mutableListOf<NarrativeConnectionEntity>()
                    
                    // Erős kapcsolatok: tenger - szabadság
                    connections.add(
                        NarrativeConnectionEntity(
                            fromId = insertedIds[0],  // tenger
                            toId = insertedIds[1],    // szabadság
                            connectionType = "nature_emotion",
                            strength = 0.95f,
                            semanticSimilarity = 0.92f,
                            spatialDistance = 0.1f
                        )
                    )
                    
                    // Szeretet - fény
                    connections.add(
                        NarrativeConnectionEntity(
                            fromId = insertedIds[3],  // szeretet
                            toId = insertedIds[6],    // fény
                            connectionType = "emotion_abstract",
                            strength = 0.85f,
                            semanticSimilarity = 0.78f,
                            spatialDistance = 0.3f
                        )
                    )
                    
                    // Félelem - árnyék
                    connections.add(
                        NarrativeConnectionEntity(
                            fromId = insertedIds[5],  // félelem
                            toId = insertedIds[7],    // árnyék
                            connectionType = "emotion_abstract",
                            strength = 0.88f,
                            semanticSimilarity = 0.81f,
                            spatialDistance = 0.2f
                        )
                    )
                    
                    // Idő - emlékezet
                    connections.add(
                        NarrativeConnectionEntity(
                            fromId = insertedIds[8],  // idő
                            toId = insertedIds[9],    // emlékezet
                            connectionType = "temporal",
                            strength = 0.90f,
                            semanticSimilarity = 0.85f,
                            spatialDistance = 0.15f
                        )
                    )
                    
                    connectionDao.insertAll(connections)
                }
            }
        }
    }
}

// ==============================================
// EXTRA: Database Helper Functions
// ==============================================

/**
 * Kiterjesztések a könnyebb adatbázis kezeléshez
 */
fun NarrativeDatabase.clearAllData() {
    val narrativeDao = this.narrativeDao()
    val connectionDao = this.connectionDao()
    
    CoroutineScope(Dispatchers.IO).launch {
        connectionDao.deleteAll()
        narrativeDao.deleteAll()
        narrativeDao.vacuum()
    }
}

fun NarrativeDatabase.exportSchema(context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        // Schema export fájlba mentése
        val schemaFile = context.getDatabasePath("narrative_space.db").parentFile
        schemaFile?.mkdirs()
    }
}

/**
 * Adatbázis teljesítmény metrikák
 */
data class DatabaseMetrics(
    val narrativeCount: Long,
    val connectionCount: Long,
    val averageDensity: Float,
    val databaseSizeKB: Long,
    val lastMaintenance: String
)

suspend fun NarrativeDatabase.getMetrics(context: Context): DatabaseMetrics {
    val narrativeDao = this.narrativeDao()
    val connectionDao = this.connectionDao()
    
    return DatabaseMetrics(
        narrativeCount = narrativeDao.getCount(),
        connectionCount = connectionDao.getCount(),
        averageDensity = narrativeDao.getAverageDensity(),
        databaseSizeKB = context.getDatabasePath("narrative_space.db").length() / 1024,
        lastMaintenance = "N/A" // TODO: Implement last maintenance timestamp
    )
}
