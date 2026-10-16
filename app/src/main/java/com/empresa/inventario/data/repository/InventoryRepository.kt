package com.empresa.inventario.data.repository

import com.empresa.inventario.data.local.ProductDao
import com.empresa.inventario.data.local.ProductEntity
import com.empresa.inventario.data.remote.FirestoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class InventoryRepository(
    private val productDao: ProductDao,
    private val firestoreManager: FirestoreManager,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    // Lectura reactiva desde Room (Offline-first)
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()

    // Búsqueda y filtrado local
    fun searchProducts(query: String): Flow<List<ProductEntity>> {
        return productDao.searchProducts(query)
    }

    // Sincronización en tiempo real: Firestore -> Room
    fun startRealtimeSync() {
        firestoreManager.listenToProducts(
            onUpdate = { cloudProducts ->
                externalScope.launch {
                    productDao.insertAll(cloudProducts)
                }
            },
            onError = { /* Gestión de errores de red */ }
        )
    }

    // CREAR / EDITAR: guarda en Room y replica en Firestore
    suspend fun saveProduct(product: ProductEntity) {
        productDao.insertOrUpdate(product)
        firestoreManager.saveProduct(product)
    }

    // ELIMINAR: borra de Room y de Firestore
    suspend fun deleteProduct(product: ProductEntity) {
        productDao.delete(product)
        firestoreManager.deleteProduct(product.id)
    }
}