package com.meaning.app.kernel

import com.meaning.app.db.NarrativeDao
import com.meaning.app.db.NarrativeConnectionDao
import com.meaning.app.db.QuantizedNarrativeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NarrativeRepository @Inject constructor(
    private val narrativeDao: NarrativeDao,
    private val connectionDao: NarrativeConnectionDao
) {
    // === ADATOLYAMOK (Flow) ===
    // Ez frissíti a térképet automatikusan, ha változik az adatbázis
    val narrativeMapStream: Flow<NarrativeMap3D> = narrativeDao.getAllStream()
        .map { entities ->
            // Itt használjuk a NarrativeMap3D "okos" generátorát
            NarrativeMap3D.fromEntities(entities)
        }

    // === LEKÉRDEZÉSEK ===
    suspend fun getEntityById(id: Long): QuantizedNarrativeEntity? {
        return narrativeDao.getById(id)
    }

    suspend fun searchByTerm(term: String): QuantizedNarrativeEntity? {
        return narrativeDao.getByTerm(term)
    }

    // "Kamera szerinti" betöltés - csak azt kéri le, ami látszik
    suspend fun loadVisiblePoints(viewBox: BoundingBox): List<QuantizedNarrativeEntity> {
        return narrativeDao.getInBoundingBox(
            viewBox.minX, viewBox.maxX,
            viewBox.minY, viewBox.maxY,
            viewBox.minZ, viewBox.maxZ
        )
    }

    // === HÁLÓZAT KUTATÁS ===
    suspend fun discoverAssociations(seedId: Long): List<Long> {
        // Ez hívja meg a rekurzív SQL lekérdezést (Community Detection)
        return connectionDao.discoverNetwork(seedId)
    }

    // === MÓDOSÍTÁSOK ===
    suspend fun addNarrativePoint(entity: QuantizedNarrativeEntity) {
        narrativeDao.insert(entity)
    }

    suspend fun resetDatabase() {
        narrativeDao.clearAll()
        connectionDao.clearAll()
    }
}
