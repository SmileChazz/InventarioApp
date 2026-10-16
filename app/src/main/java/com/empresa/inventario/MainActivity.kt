package com.empresa.inventario

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.empresa.inventario.data.local.ProductEntity
import com.empresa.inventario.ui.InventoryViewModel
import com.empresa.inventario.ui.ProductAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private val viewModel: InventoryViewModel by viewModels()
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etSearch = findViewById<EditText>(R.id.etSearch)
        val rvProducts = findViewById<RecyclerView>(R.id.rvProducts)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)

        adapter = ProductAdapter(
            onItemClick = { product -> showProductDialog(product) },
            onDeleteClick = { product -> confirmDelete(product) }
        )

        rvProducts.layoutManager = LinearLayoutManager(this)
        rvProducts.adapter = adapter

        // Búsqueda en tiempo real
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onSearchQueryChanged(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Observar lista de productos
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.products.collect { list ->
                    adapter.submitList(list)
                }
            }
        }

        // Botón flotante para agregar producto
        fabAdd.setOnClickListener {
            showProductDialog(null)
        }
    }

    // Cuadro de diálogo para Crear (null) o Editar (con producto existente)
    private fun showProductDialog(product: ProductEntity?) {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val etName = EditText(context).apply { hint = "Nombre"; setText(product?.name ?: "") }
        val etDesc = EditText(context).apply { hint = "Descripción"; setText(product?.description ?: "") }
        val etQuantity = EditText(context).apply {
            hint = "Cantidad"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(product?.quantity?.toString() ?: "")
        }
        val etPrice = EditText(context).apply {
            hint = "Precio"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(product?.price?.toString() ?: "")
        }
        val etSupplier = EditText(context).apply { hint = "Proveedor"; setText(product?.supplier ?: "") }

        layout.addView(etName)
        layout.addView(etDesc)
        layout.addView(etQuantity)
        layout.addView(etPrice)
        layout.addView(etSupplier)

        AlertDialog.Builder(context)
            .setTitle(if (product == null) "Nuevo Producto" else "Editar Producto")
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                val name = etName.text.toString().trim()
                val desc = etDesc.text.toString().trim()
                val qty = etQuantity.text.toString().toIntOrNull() ?: 0
                val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0
                val supplier = etSupplier.text.toString().trim()

                if (name.isNotEmpty()) {
                    val itemToSave = product?.copy(
                        name = name,
                        description = desc,
                        quantity = qty,
                        price = price,
                        supplier = supplier,
                        updatedAt = System.currentTimeMillis()
                    ) ?: ProductEntity(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        description = desc,
                        quantity = qty,
                        price = price,
                        supplier = supplier
                    )
                    viewModel.saveProduct(itemToSave)
                } else {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmDelete(product: ProductEntity) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás segura de eliminar '${product.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteProduct(product)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
