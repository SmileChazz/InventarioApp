package com.empresa.inventario.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val supplier: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    // No-argument constructor required by Firebase Firestore deserialization
    constructor() : this("", "", "", 0, 0.0, "", System.currentTimeMillis())
}
