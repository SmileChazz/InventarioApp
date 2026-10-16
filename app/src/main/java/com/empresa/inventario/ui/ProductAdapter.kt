package com.empresa.inventario.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.empresa.inventario.R
import com.empresa.inventario.data.local.ProductEntity

class ProductAdapter(
    private val onItemClick: (ProductEntity) -> Unit,
    private val onDeleteClick: (ProductEntity) -> Unit
) : ListAdapter<ProductEntity, ProductAdapter.ProductViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvProductDesc)
        private val tvSupplier: TextView = itemView.findViewById(R.id.tvSupplier)
        private val tvStockAndPrice: TextView = itemView.findViewById(R.id.tvStockAndPrice)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(product: ProductEntity) {
            tvName.text = product.name
            tvDesc.text = product.description
            tvSupplier.text = "Proveedor: ${product.supplier}"
            tvStockAndPrice.text = "Stock: ${product.quantity} un. | $${product.price}"

            itemView.setOnClickListener { onItemClick(product) }
            btnDelete.setOnClickListener { onDeleteClick(product) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ProductEntity>() {
        override fun areItemsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean =
            oldItem == newItem
    }
}