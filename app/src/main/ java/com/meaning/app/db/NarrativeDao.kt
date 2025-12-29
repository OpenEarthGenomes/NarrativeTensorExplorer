package com.meaning.app.db
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface NarrativeDao {
    @Query("SELECT * FROM narratives ORDER BY timestamp DESC")
    fun getAllNarratives(): Flow<List<NarrativeEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NarrativeEntity)
}
