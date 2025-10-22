package com.seu_pacote.activity

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.seu_pacote.R

class HomePageActivity : AppCompatActivity() {

    // Declarações dos componentes de UI
    private lateinit var menuButton: LinearLayout
    private lateinit var scrimView: View
    private lateinit var menuPanel: FrameLayout

    private var isMenuOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        // Inicializa as views do layout
        menuButton = findViewById(R.id.menu_button)
        scrimView = findViewById(R.id.scrim_view)
        menuPanel = findViewById(R.id.menu_panel)

        // Configura o listener de clique para o botão de menu
        menuButton.setOnClickListener {
            toggleMenu()
        }

        // Configura o listener para o fundo escurecido, para fechar o menu ao clicar fora
        scrimView.setOnClickListener {
            if (isMenuOpen) {
                toggleMenu()
            }
        }
        
        // Inicializa o estado do menu (escondido)
        prepareMenu()
    }

    private fun prepareMenu() {
        // Move o painel do menu para fora da tela (à esquerda)
        val screenWidth = resources.displayMetrics.widthPixels
        menuPanel.translationX = -screenWidth.toFloat()
    }
    
    private fun toggleMenu() {
        isMenuOpen = !isMenuOpen

        if (isMenuOpen) {
            // MOSTRA O MENU
            scrimView.visibility = View.VISIBLE
            menuPanel.visibility = View.VISIBLE

            // Animação do fundo escurecido (aparece suavemente)
            ObjectAnimator.ofFloat(scrimView, "alpha", 0f, 1f).apply {
                duration = 300
                start()
            }

            // Animação do painel do menu (desliza para a posição 0)
            ObjectAnimator.ofFloat(menuPanel, "translationX", -menuPanel.width.toFloat(), 0f).apply {
                duration = 300
                start()
            }
        } else {
            // ESCONDE O MENU
            // Animação do fundo escurecido (desaparece suavemente)
            ObjectAnimator.ofFloat(scrimView, "alpha", 1f, 0f).apply {
                duration = 300
                start()
            }

            // Animação do painel do menu (desliza para fora da tela)
            ObjectAnimator.ofFloat(menuPanel, "translationX", 0f, -menuPanel.width.toFloat()).apply {
                duration = 300
                // Adiciona um listener para tornar as views 'gone' ao final da animação
                doOnEnd {
                    scrimView.visibility = View.GONE
                    menuPanel.visibility = View.GONE
                }
                start()
            }
        }
    }
    
    // Sobrescreve o botão "Voltar" do Android para fechar o menu se estiver aberto
    override fun onBackPressed() {
        if (isMenuOpen) {
            toggleMenu()
        } else {
            super.onBackPressed()
        }
    }
}
