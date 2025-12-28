package com.meaning.app.db

import androidx.paging.PagingSource
import androidx.room.*

@Dao
interface NarrativeDao {
    @Query("SELECT * FROM quantized_narrative ORDER BY term ASC")
    fun getAllPaged(): PagingSource<Int, QuantizedNarrativeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<QuantizedNarrativeEntity>)

    @Query("SELECT * FROM quantized_narrative WHERE id = :id")
    suspend fun getById(id: Int): QuantizedNarrativeEntity?
}

