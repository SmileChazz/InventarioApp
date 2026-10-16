package com.empresa.inventario.data.remote

import com.empresa.inventario.data.local.ProductEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class FirestoreManager {

    private val firestore = FirebaseFirestore.getInstance()
    private val productsCollection = firestore.collection("products")

    // CREAR / EDITAR en Firebase
    suspend fun saveProduct(product: ProductEntity): Result<Unit> {
        return try {
            productsCollection.document(product.id).set(product).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // LEER en tiempo real (Listener continuo)
    fun listenToProducts(
        onUpdate: (List<ProductEntity>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return productsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ProductEntity::class.java)
                }
                onUpdate(list)
            }
        }
    }

    // ELIMINAR en Firebase
    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            productsCollection.document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}