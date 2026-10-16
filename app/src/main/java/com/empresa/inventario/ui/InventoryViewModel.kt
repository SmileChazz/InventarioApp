package com.empresa.inventario.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.empresa.inventario.data.local.AppDatabase
import com.empresa.inventario.data.local.ProductEntity
import com.empresa.inventario.data.remote.FirestoreManager
import com.empresa.inventario.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InventoryRepository
    private val searchQuery = MutableStateFlow("")

    val products: StateFlow<List<ProductEntity>>

    init {
        val database = AppDatabase.getDatabase(application)
        val firestoreManager = FirestoreManager()
        repository = InventoryRepository(database.productDao(), firestoreManager)

        // Inicia el listener de Firebase al arrancar
        repository.startRealtimeSync()

        // Filtrado dinámico
        products = searchQuery.flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allProducts
            } else {
                repository.searchProducts(query)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun onSearchQueryChanged(newQuery: String) {
        searchQuery.value = newQuery
    }

    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.saveProduct(product)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }
}