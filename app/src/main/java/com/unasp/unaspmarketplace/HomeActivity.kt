package com.unasp.unaspmarketplace

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unasp.unaspmarketplace.modelos.Category
import com.unasp.unaspmarketplace.modelos.CategoryAdapter
import com.unasp.unaspmarketplace.modelos.Product
import com.unasp.unaspmarketplace.modelos.ProductAdapter

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        val categorys = listOf(
            Category("Roupas", R.drawable.tshirt_logo),
            Category("Eletrônicos", R.drawable.computer_logo),
            Category("Alimentos", R.drawable.apple_logo),
            Category("Livros", R.drawable.book_logo)
        )

        val recyclerCategory = findViewById<RecyclerView>(R.id.recyclerCategorys)
        recyclerCategory.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerCategory.adapter = CategoryAdapter(categorys)

        // Lista de produtos mock
        val produtos = listOf(
            Product("Notebook Dell", 3500.0, R.drawable.note_dell),
            Product("Camiseta Azul", 79.9, R.drawable.tshit_blue_nike),
            Product("Livro Kotlin", 120.0, R.drawable.book_kotlin),
            Product("Fone Bluetooth", 250.0, R.drawable.fone_apple)
        )

        val recyclerProduct = findViewById<RecyclerView>(R.id.recyclerProducts)
        recyclerProduct.layoutManager = GridLayoutManager(this, 2)
        recyclerProduct.adapter = ProductAdapter(produtos)
    }
}