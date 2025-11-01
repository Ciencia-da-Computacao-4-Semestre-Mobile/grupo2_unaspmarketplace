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
import android.content.Intent
import androidx.drawerlayout.widget.DrawerLayout
import android.widget.Toast
import com.google.android.material.navigation.NavigationView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.view.GravityCompat

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
        recyclerCategory.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerCategory.adapter = CategoryAdapter(categorys)

        val produtos = listOf(
            Product("Notebook Dell", 3500.0, R.drawable.note_dell),
            Product("Camiseta Azul", 79.9, R.drawable.tshit_blue_nike),
            Product("Livro Kotlin", 120.0, R.drawable.book_kotlin),
            Product("Fone Bluetooth", 250.0, R.drawable.fone_apple)
        )

        val recyclerProduct = findViewById<RecyclerView>(R.id.recyclerProducts)
        recyclerProduct.layoutManager = GridLayoutManager(this, 2)
        recyclerProduct.adapter = ProductAdapter(produtos)

        // Configuração do menu lateral
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)

        // Verificar se deve abrir o menu automaticamente
        if (intent.getBooleanExtra("openMenu", false)) {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Configuração da hotbar inferior
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_menu -> {
                    // Abre o menu lateral
                    drawerLayout.openDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_home -> {
                    // Já estamos na home, não precisa fazer nada
                    Toast.makeText(this, "Você já está na Home", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_notifications -> {
                    // Implementar navegação para notificações
                    Toast.makeText(this, "Notificações em breve", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_cart -> {
                    // Navegar para o carrinho
                    val intent = Intent(this, CartActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Configuração do menu lateral
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_post_item -> {
                    val intent = Intent(this, PostItemActivity::class.java)
                    startActivity(intent)
                }

                R.id.nav_profile -> {
                    Toast.makeText(this, "Perfil", Toast.LENGTH_SHORT).show()
                }
            }
            drawerLayout.closeDrawers() // fecha o menu depois do clique
            true
        }
    }
}