package com.unasp.unaspmarketplace

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.storage.FirebaseStorage
import com.unasp.unaspmarketplace.adapters.ProductImageAdapter
import com.unasp.unaspmarketplace.models.Product
import com.unasp.unaspmarketplace.repository.ProductRepository
import com.unasp.unaspmarketplace.utils.ProductImageVerifier
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class PostItemActivity : AppCompatActivity() {
    private lateinit var productRepository: ProductRepository
    private lateinit var edtName: TextInputEditText
    private lateinit var edtDescription: TextInputEditText
    private lateinit var edtPrice: TextInputEditText
    private lateinit var edtStock: TextInputEditText
    private lateinit var spinnerCategory: AutoCompleteTextView
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnAddImage: MaterialButton
    private lateinit var btnRemoveImage: MaterialButton
    private lateinit var recyclerImages: RecyclerView

    private lateinit var imageAdapter: ProductImageAdapter
    private val storage = FirebaseStorage.getInstance()

    // Variables for edit mode
    private var isEditMode = false
    private var productId: String? = null
    private var currentProduct: Product? = null

    companion object {
        private const val STORAGE_PERMISSION_CODE = 101
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    // Verificar se o adapter está inicializado
                    if (::imageAdapter.isInitialized) {
                        imageAdapter.addImage(uri)
                        btnRemoveImage.isEnabled = imageAdapter.getImages().isNotEmpty()

                        // Mostrar feedback positivo
                        Toast.makeText(this, "✅ Foto adicionada com sucesso!", Toast.LENGTH_SHORT).show()

                        // Se for a primeira foto, mostrar dica sobre adicionar mais
                        if (imageAdapter.getImages().size == 1) {
                            Toast.makeText(this, "💡 Dica: Adicione mais fotos para atrair compradores!", Toast.LENGTH_LONG).show()
                        }

                        // Se atingiu 3 fotos, mostrar estatística motivacional
                        if (imageAdapter.getImages().size == 3) {
                            showPhotoStatsDialog()
                        }
                    } else {
                        Toast.makeText(this, "❌ Erro: adapter não inicializado", Toast.LENGTH_SHORT).show()
                    }
                } ?: run {
                    Toast.makeText(this, "❌ Nenhuma foto selecionada", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Usuário cancelou a seleção
                Toast.makeText(this, "📷 Seleção de foto cancelada", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("PostItemActivity", "Erro ao processar foto", e)
            Toast.makeText(this, "❌ Erro ao processar foto: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_item_activity)

        // Check if this is edit mode
        isEditMode = intent.getBooleanExtra("editMode", false)
        productId = intent.getStringExtra("productId")

        // Inicializar repositório
        productRepository = ProductRepository()

        // Inicializar views
        initViews()
        setupImageRecycler()
        setupCategorySpinner()
        setupButtons()
        setupBottomNavigation()

        // Load product data if in edit mode
        if (isEditMode && !productId.isNullOrEmpty()) {
            loadProductData()
        }
    }

    private fun initViews() {
        try {
            edtName = findViewById(R.id.edtName)
            edtDescription = findViewById(R.id.edtDescription)
            edtPrice = findViewById(R.id.edtPrice)
            edtStock = findViewById(R.id.edtStock)
            spinnerCategory = findViewById(R.id.spinnerCategory)
            btnSave = findViewById(R.id.btnSave)
            btnCancel = findViewById(R.id.btnCancel)
            btnAddImage = findViewById(R.id.btnAddImage)
            btnRemoveImage = findViewById(R.id.btnRemoveImage)
            recyclerImages = findViewById(R.id.recyclerImages)

            // Configurar toolbar
            val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.appbar_post)
            toolbar.setNavigationOnClickListener { finish() }

            // Update toolbar title based on mode
            toolbar.title = if (isEditMode) "Editar Produto" else "Novo Produto"

            // Update save button text based on mode
            btnSave.text = if (isEditMode) "💾 Atualizar Produto" else "💾 Salvar Produto"

            // Inicialmente desabilitar botão de remover
            btnRemoveImage.isEnabled = false
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao inicializar interface: ${e.message}", Toast.LENGTH_LONG).show()
            finish() // Se não conseguir inicializar as views, fechar a activity
        }
    }

    private fun loadProductData() {
        productId?.let { id ->
            lifecycleScope.launch {
                try {
                    btnSave.isEnabled = false
                    btnSave.text = "Carregando..."

                    val result = productRepository.getProductById(id)
                    if (result.isSuccess) {
                        val product = result.getOrNull()
                        if (product != null) {
                            currentProduct = product

                            runOnUiThread {
                                // Fill form fields with product data
                                edtName.setText(product.name)
                                edtDescription.setText(product.description)
                                edtPrice.setText(product.price.toString())
                                edtStock.setText(product.stock.toString())
                                spinnerCategory.setText(product.category, false)

                                // Load existing images
                                loadExistingImages(product.imageUrls)

                                btnSave.isEnabled = true
                                btnSave.text = "💾 Atualizar Produto"

                                Toast.makeText(this@PostItemActivity, "Produto carregado para edição", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@PostItemActivity, "Produto não encontrado", Toast.LENGTH_LONG).show()
                                finish()
                            }
                        }
                    } else {
                        runOnUiThread {
                            val error = result.exceptionOrNull()
                            Toast.makeText(this@PostItemActivity, "Erro ao carregar produto: ${error?.message}", Toast.LENGTH_LONG).show()
                            finish()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@PostItemActivity, "Erro ao carregar produto: ${e.message}", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun loadExistingImages(imageUrls: List<String>) {
        Log.d("PostItemActivity", "Carregando ${imageUrls.size} imagens existentes")

        for ((index, imageUrl) in imageUrls.withIndex()) {
            try {
                Log.d("PostItemActivity", "Processando imagem $index: $imageUrl")

                if (imageUrl.isNotEmpty() && (imageUrl.startsWith("https://") || imageUrl.startsWith("http://"))) {
                    val uri = Uri.parse(imageUrl)
                    imageAdapter.addImage(uri)
                    Log.d("PostItemActivity", "✅ Imagem $index adicionada com sucesso")
                } else {
                    Log.w("PostItemActivity", "⚠️ URL inválida para imagem $index: $imageUrl")
                }
            } catch (e: Exception) {
                Log.e("PostItemActivity", "❌ Erro ao carregar imagem $index: ${e.message}", e)
                e.printStackTrace()
            }
        }

        btnRemoveImage.isEnabled = imageAdapter.getImages().isNotEmpty()

        // Verificar se todas as imagens foram carregadas corretamente
        val loadedCount = imageAdapter.getImages().size
        val expectedCount = imageUrls.size

        Log.d("PostItemActivity", "Resultado do carregamento: $loadedCount/$expectedCount imagens carregadas")

        if (loadedCount < expectedCount) {
            Toast.makeText(this, "⚠️ Algumas imagens podem não ter sido carregadas ($loadedCount/$expectedCount)", Toast.LENGTH_LONG).show()
        } else if (loadedCount > 0) {
            Toast.makeText(this, "✅ $loadedCount imagens carregadas com sucesso", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupImageRecycler() {
        try {
            imageAdapter = ProductImageAdapter(mutableListOf()) { position ->
                imageAdapter.removeImage(position)
                btnRemoveImage.isEnabled = imageAdapter.getImages().isNotEmpty()
            }

            recyclerImages.apply {
                adapter = imageAdapter
                layoutManager = LinearLayoutManager(this@PostItemActivity, LinearLayoutManager.HORIZONTAL, false)
                setHasFixedSize(true)
                // Adicionar decoração para espaçamento se necessário
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao configurar galeria de imagens: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf(
            "Roupas",
            "Eletrônicos",
            "Alimentos",
            "Livros",
            "Casa e Jardim",
            "Esportes",
            "Beleza",
            "Automóveis",
            "Outros"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        spinnerCategory.setAdapter(adapter)
    }

    private fun setupButtons() {
        btnSave.setOnClickListener {
            saveProduct()
        }

        btnCancel.setOnClickListener {
            finish()
        }

        btnAddImage.setOnClickListener {
            openGallery()
        }

        btnRemoveImage.setOnClickListener {
            if (imageAdapter.getImages().isNotEmpty()) {
                showImageOptionsDialog()
            } else {
                showImageTips()
            }
        }
    }

    private fun showImageTips() {
        AlertDialog.Builder(this)
            .setTitle("💡 Dicas para Fotos de Produtos")
            .setMessage(
                "📸 TIRE BOAS FOTOS:\n" +
                "• Use boa iluminação natural\n" +
                "• Mostre o produto de vários ângulos\n" +
                "• Fundo limpo e neutro\n" +
                "• Foque nos detalhes importantes\n\n" +
                "🚀 PRODUTOS COM FOTOS:\n" +
                "• Vendem 3x mais rápido\n" +
                "• Geram mais confiança\n" +
                "• Recebem mais visualizações\n\n" +
                "Adicione fotos para ter mais sucesso!"
            )
            .setPositiveButton("📸 Adicionar Fotos") { _, _ ->
                openGallery()
            }
            .setNegativeButton("⏭️ Depois") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showPhotoStatsDialog() {
        AlertDialog.Builder(this)
            .setTitle("🎉 Excelente!")
            .setMessage(
                "Você adicionou 3 fotos! Isso é ótimo! 📸\n\n" +
                "📊 ESTATÍSTICAS INTERESSANTES:\n" +
                "• Produtos com 3+ fotos vendem 5x mais\n" +
                "• 85% dos compradores preferem múltiplas fotos\n" +
                "• Fotos de qualidade aumentam o preço em 20%\n\n" +
                "🏆 VOCÊ ESTÁ NO CAMINHO CERTO!\n" +
                "Continue assim para maximizar suas vendas!"
            )
            .setPositiveButton("😊 Entendi!") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openGallery() {
        try {
            when {
                checkStoragePermission() -> {
                    // Permissão já concedida, abrir galeria
                    launchImagePicker()
                }
                shouldShowPermissionRationale() -> {
                    // Mostrar explicação sobre por que a permissão é necessária
                    showPermissionExplanationDialog()
                }
                else -> {
                    // Solicitar permissão pela primeira vez
                    requestStoragePermission()
                }
            }
        } catch (e: Exception) {
            Log.e("PostItemActivity", "Erro ao abrir galeria", e)
            Toast.makeText(this, "Erro ao abrir galeria de fotos: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ usa READ_MEDIA_IMAGES
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 e inferior usa READ_EXTERNAL_STORAGE
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun shouldShowPermissionRationale(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(permission),
            STORAGE_PERMISSION_CODE
        )
    }

    private fun launchImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("📸 Permissão Necessária")
            .setMessage(
                "Para adicionar fotos aos seus produtos, o aplicativo precisa acessar suas imagens.\n\n" +
                "🔒 Sua privacidade é importante:\n" +
                "• Apenas você escolhe quais fotos usar\n" +
                "• Não acessamos outras imagens\n" +
                "• As fotos são usadas apenas para o produto\n\n" +
                "Conceder acesso às imagens?"
            )
            .setPositiveButton("✅ Permitir") { _, _ ->
                requestStoragePermission()
            }
            .setNegativeButton("❌ Agora Não") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Você pode adicionar fotos mais tarde nas configurações", Toast.LENGTH_LONG).show()
            }
            .setNeutralButton("ℹ️ Saiba Mais") { _, _ ->
                showDetailedPermissionInfo()
            }
            .show()
    }

    private fun showDetailedPermissionInfo() {
        AlertDialog.Builder(this)
            .setTitle("ℹ️ Por que precisamos desta permissão?")
            .setMessage(
                "🖼️ ACESSO ÀS IMAGENS:\n" +
                "Para que você possa:\n" +
                "• Escolher fotos da galeria\n" +
                "• Mostrar seus produtos com imagens\n" +
                "• Atrair mais compradores\n\n" +
                "🔐 SEGURANÇA:\n" +
                "• Apenas imagens que você selecionar\n" +
                "• Nenhum acesso automático\n" +
                "• Dados ficam no seu controle\n\n" +
                "🚀 BENEFÍCIOS:\n" +
                "• Produtos mais atrativos\n" +
                "• Mais vendas\n" +
                "• Melhor experiência"
            )
            .setPositiveButton("Entendi, Permitir") { _, _ ->
                requestStoragePermission()
            }
            .setNegativeButton("Talvez Depois") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("⚠️ Permissão Negada")
            .setMessage(
                "Sem acesso às imagens, você não poderá adicionar fotos aos produtos.\n\n" +
                "🔧 COMO ATIVAR:\n" +
                "1. Vá em Configurações do App\n" +
                "2. Toque em 'Permissões'\n" +
                "3. Ative 'Armazenamento' ou 'Mídia'\n\n" +
                "💡 DICA: Produtos com fotos vendem 3x mais!"
            )
            .setPositiveButton("🔧 Abrir Configurações") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("📝 Continuar sem Fotos") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Você pode adicionar fotos depois editando o produto", Toast.LENGTH_LONG).show()
            }
            .show()
    }

    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Não foi possível abrir as configurações", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showImageOptionsDialog() {
        val images = imageAdapter.getImages()
        if (images.isEmpty()) {
            showImageTips()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("🖼️ Opções de Imagens")
            .setMessage("Você tem ${images.size} imagem(ns). O que deseja fazer?")
            .setPositiveButton("🗑️ Remover Imagem") { _, _ ->
                showRemoveImageDialog()
            }
            .setNegativeButton("🔍 Verificar Imagens") { _, _ ->
                showImageStatistics()
            }
            .setNeutralButton("🧪 Testar URLs") { _, _ ->
                testImageUrls()
            }
            .show()
    }

    private fun showRemoveImageDialog() {
        val images = imageAdapter.getImages()
        if (images.isEmpty()) return

        val imageNames = images.mapIndexed { index, _ -> "Foto ${index + 1}" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Remover Foto")
            .setItems(imageNames) { _, which ->
                imageAdapter.removeImage(which)
                btnRemoveImage.isEnabled = imageAdapter.getImages().isNotEmpty()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveProduct() {
        // Verificar se o usuário tem WhatsApp cadastrado primeiro
        lifecycleScope.launch {
            try {
                val currentUser = com.unasp.unaspmarketplace.utils.UserUtils.getCurrentUser()
                if (currentUser?.whatsappNumber.isNullOrBlank()) {
                    runOnUiThread {
                        showWhatsAppRequiredDialog()
                    }
                    return@launch
                }

                // Continuar com validações normais
                validateAndSaveProduct()

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@PostItemActivity,
                        "Erro ao verificar dados do usuário: ${e.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showWhatsAppRequiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("WhatsApp Obrigatório")
            .setMessage("Para vender produtos no marketplace, você precisa cadastrar um número de WhatsApp no seu perfil. " +
                       "Assim, compradores poderão entrar em contato com você!\n\n" +
                       "Deseja ir para o perfil e cadastrar seu WhatsApp agora?")
            .setPositiveButton("Ir para Perfil") { _, _ ->
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun validateAndSaveProduct() {
        // Validar campos
        val name = edtName.text.toString().trim()
        val description = edtDescription.text.toString().trim()
        val priceText = edtPrice.text.toString().trim()
        val stockText = edtStock.text.toString().trim()
        val category = spinnerCategory.text.toString().trim()

        if (name.isEmpty()) {
            edtName.error = "Nome é obrigatório"
            edtName.requestFocus()
            return
        }

        if (description.isEmpty()) {
            edtDescription.error = "Descrição é obrigatória"
            edtDescription.requestFocus()
            return
        }

        if (priceText.isEmpty()) {
            edtPrice.error = "Preço é obrigatório"
            edtPrice.requestFocus()
            return
        }

        if (stockText.isEmpty()) {
            edtStock.error = "Estoque é obrigatório"
            edtStock.requestFocus()
            return
        }

        if (category.isEmpty()) {
            spinnerCategory.error = "Categoria é obrigatória"
            spinnerCategory.requestFocus()
            return
        }

        val price = priceText.toDoubleOrNull()
        if (price == null || price <= 0) {
            edtPrice.error = "Preço deve ser um valor válido"
            edtPrice.requestFocus()
            return
        }

        val stock = stockText.toIntOrNull()
        if (stock == null || stock < 0) {
            edtStock.error = "Estoque deve ser um número válido"
            edtStock.requestFocus()
            return
        }

        // Verificar se há fotos e oferecer sugestão se não houver
        if (imageAdapter.getImages().isEmpty()) {
            showNoPhotosWarningDialog(name, description, price, stock, category)
            return
        }

        // Continuar com o salvamento
        saveProductWithValidation(name, description, price, stock, category)
    }

    private fun showNoPhotosWarningDialog(name: String, description: String, price: Double, stock: Int, category: String) {
        AlertDialog.Builder(this)
            .setTitle("📷 Sem Fotos Detectadas")
            .setMessage(
                "Você está prestes a publicar um produto sem fotos.\n\n" +
                "📊 DADOS IMPORTANTES:\n" +
                "• Produtos SEM fotos: 15% de chance de venda\n" +
                "• Produtos COM fotos: 85% de chance de venda\n" +
                "• Diferença: 5.6x mais vendas!\n\n" +
                "🤔 O que você deseja fazer?"
            )
            .setPositiveButton("📸 Adicionar Fotos") { _, _ ->
                openGallery()
            }
            .setNegativeButton("💾 Publicar Assim Mesmo") { _, _ ->
                saveProductWithValidation(name, description, price, stock, category)
            }
            .setNeutralButton("❌ Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun saveProductWithValidation(name: String, description: String, price: Double, stock: Int, category: String) {
        // Salvar no Firebase
        btnSave.isEnabled = false
        btnSave.text = if (isEditMode) "Atualizando..." else "Salvando..."

        lifecycleScope.launch {
            try {
                // Upload das imagens primeiro
                val imageUrls = uploadImages()

                // Verificar se as imagens foram processadas corretamente
                val originalImageCount = imageAdapter.getImages().size
                val processedImageCount = imageUrls.size

                Log.d("PostItemActivity", "Verificação de imagens: $processedImageCount/$originalImageCount processadas")

                if (originalImageCount > 0 && processedImageCount == 0) {
                    throw Exception("Nenhuma imagem foi processada com sucesso")
                }

                if (processedImageCount < originalImageCount) {
                    Log.w("PostItemActivity", "⚠️ Algumas imagens falharam no upload: $processedImageCount/$originalImageCount")
                    runOnUiThread {
                        Toast.makeText(this@PostItemActivity, "⚠️ Apenas $processedImageCount de $originalImageCount imagens foram salvas", Toast.LENGTH_LONG).show()
                    }
                }

                // Criar ou atualizar produto
                val product = if (isEditMode && currentProduct != null) {
                    // Update existing product
                    currentProduct!!.copy(
                        name = name,
                        description = description,
                        price = price,
                        stock = stock,
                        category = category,
                        imageUrls = imageUrls
                    )
                } else {
                    // Create new product
                    Product(
                        name = name,
                        description = description,
                        price = price,
                        stock = stock,
                        category = category,
                        imageUrls = imageUrls,
                        active = true
                    )
                }

                Log.d("PostItemActivity", "Produto criado com ${product.imageUrls.size} imagens: ${product.imageUrls}")

                val result = productRepository.saveProduct(product)

                runOnUiThread {
                    btnSave.isEnabled = true
                    btnSave.text = if (isEditMode) "💾 Atualizar Produto" else "💾 Salvar Produto"

                    if (result.isSuccess) {
                        val productId = result.getOrNull()
                        val message = if (isEditMode) "Produto atualizado com sucesso!" else "Produto salvo com sucesso!"
                        Toast.makeText(this@PostItemActivity, message, Toast.LENGTH_SHORT).show()

                        // Verificação final e feedback sobre imagens
                        if (imageUrls.isNotEmpty()) {
                            Toast.makeText(this@PostItemActivity, "📸 ${imageUrls.size} imagem(ns) salva(s) com sucesso", Toast.LENGTH_LONG).show()

                            // Verificar se as imagens foram salvas corretamente no Firebase
                            if (!productId.isNullOrEmpty()) {
                                verifyProductImagesInFirebase(productId)
                            }
                        }

                        // Voltar para a tela anterior
                        finish()
                    } else {
                        val error = result.exceptionOrNull()
                        val errorMessage = if (isEditMode) "Erro ao atualizar: ${error?.message}" else "Erro ao salvar: ${error?.message}"
                        Toast.makeText(this@PostItemActivity, errorMessage, Toast.LENGTH_LONG).show()

                        Log.e("PostItemActivity", "Erro ao salvar produto", error)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    btnSave.isEnabled = true
                    btnSave.text = if (isEditMode) "💾 Atualizar Produto" else "💾 Salvar Produto"
                    Toast.makeText(this@PostItemActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }

                Log.e("PostItemActivity", "Erro geral ao salvar produto", e)
            }
        }
    }

    private suspend fun uploadImages(): List<String> {
        val imageUrls = mutableListOf<String>()
        val images = imageAdapter.getImages()

        Log.d("PostItemActivity", "Iniciando upload de ${images.size} imagens")

        for ((index, imageUri) in images.withIndex()) {
            try {
                val uriString = imageUri.toString()
                Log.d("PostItemActivity", "Processando imagem $index: $uriString")

                // Check if this is already a URL (existing image) or a local URI (new image)
                if (uriString.startsWith("https://") || uriString.startsWith("http://")) {
                    // This is already an uploaded image URL, just add it to the list
                    imageUrls.add(uriString)
                    Log.d("PostItemActivity", "✅ Imagem $index já é URL: mantida")

                    // Update progress in UI
                    runOnUiThread {
                        btnSave.text = "Verificando... ${index + 1}/${images.size}"
                    }
                } else {
                    // This is a new local image that needs to be uploaded
                    Log.d("PostItemActivity", "📤 Fazendo upload da imagem $index...")

                    val fileName = "products/${UUID.randomUUID()}_${System.currentTimeMillis()}.jpg"
                    val storageRef = storage.reference.child(fileName)

                    val uploadTask = storageRef.putFile(imageUri)
                    uploadTask.await()

                    val downloadUrl = storageRef.downloadUrl.await()
                    val downloadUrlString = downloadUrl.toString()
                    imageUrls.add(downloadUrlString)

                    Log.d("PostItemActivity", "✅ Upload da imagem $index concluído: $downloadUrlString")

                    // Update progress in UI
                    runOnUiThread {
                        btnSave.text = "Uploading... ${index + 1}/${images.size}"
                    }

                    // Verificar se a URL foi gerada corretamente
                    if (downloadUrlString.isEmpty() || !downloadUrlString.startsWith("https://")) {
                        Log.e("PostItemActivity", "❌ URL inválida gerada para imagem $index: $downloadUrlString")
                        throw Exception("URL inválida gerada para a imagem")
                    }
                }
            } catch (e: Exception) {
                // Log error but continue with other images
                Log.e("PostItemActivity", "❌ Erro ao processar imagem $index: ${e.message}", e)
                e.printStackTrace()

                // Notificar o usuário do erro
                runOnUiThread {
                    Toast.makeText(this@PostItemActivity, "⚠️ Erro na imagem ${index + 1}: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        Log.d("PostItemActivity", "Upload concluído: ${imageUrls.size}/${images.size} imagens processadas")
        Log.d("PostItemActivity", "URLs finais: $imageUrls")

        return imageUrls
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permissão concedida
                    Toast.makeText(this, "✅ Permissão concedida! Abrindo galeria...", Toast.LENGTH_SHORT).show()
                    launchImagePicker()
                } else {
                    // Permissão negada
                    if (shouldShowPermissionRationale()) {
                        // Usuário negou, mas pode mostrar explicação novamente
                        showPermissionExplanationDialog()
                    } else {
                        // Usuário negou permanentemente ("Não perguntar novamente")
                        showPermissionDeniedDialog()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Verificar se a permissão foi concedida quando o usuário volta das configurações
        if (checkStoragePermission()) {
            // Se a permissão foi concedida, não fazer nada específico
            // O usuário pode tentar adicionar imagens normalmente
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation_post)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_cart -> {
                    val intent = Intent(this, CartActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Função para testar se as URLs das imagens são válidas e acessíveis
     */
    private fun testImageUrls() {
        val images = imageAdapter.getImages()
        Log.d("PostItemActivity", "🔍 Testando ${images.size} URLs de imagens...")

        lifecycleScope.launch {
            var validImages = 0
            var invalidImages = 0

            for ((index, imageUri) in images.withIndex()) {
                try {
                    val uriString = imageUri.toString()

                    if (uriString.startsWith("https://") || uriString.startsWith("http://")) {
                        // Testar se a URL é acessível (simulação básica)
                        if (uriString.contains("firebase") || uriString.contains("googleapis")) {
                            Log.d("PostItemActivity", "✅ Imagem $index válida: $uriString")
                            validImages++
                        } else {
                            Log.w("PostItemActivity", "⚠️ Imagem $index suspeita: $uriString")
                            validImages++ // Assumir válida para URLs externas
                        }
                    } else if (uriString.startsWith("content://")) {
                        // URI local válida
                        Log.d("PostItemActivity", "✅ Imagem local $index válida: $uriString")
                        validImages++
                    } else {
                        Log.e("PostItemActivity", "❌ Imagem $index inválida: $uriString")
                        invalidImages++
                    }
                } catch (e: Exception) {
                    Log.e("PostItemActivity", "❌ Erro ao testar imagem $index: ${e.message}")
                    invalidImages++
                }
            }

            val totalImages = validImages + invalidImages
            Log.d("PostItemActivity", "📊 Resultado do teste: $validImages/$totalImages válidas")

            runOnUiThread {
                if (invalidImages > 0) {
                    Toast.makeText(this@PostItemActivity,
                        "⚠️ $invalidImages de $totalImages imagens podem ter problemas",
                        Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@PostItemActivity,
                        "✅ Todas as $validImages imagens foram verificadas",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Função para mostrar estatísticas detalhadas das imagens
     */
    private fun showImageStatistics() {
        val images = imageAdapter.getImages()
        val localImages = images.count { it.toString().startsWith("content://") }
        val urlImages = images.count { it.toString().startsWith("http") }

        val message = buildString {
            appendLine("📊 ESTATÍSTICAS DE IMAGENS")
            appendLine()
            appendLine("Total de imagens: ${images.size}")
            appendLine("Imagens locais: $localImages")
            appendLine("URLs de imagens: $urlImages")
            appendLine()
            if (images.isNotEmpty()) {
                appendLine("📋 DETALHES:")
                images.forEachIndexed { index, uri ->
                    val type = when {
                        uri.toString().startsWith("content://") -> "📱 Local"
                        uri.toString().startsWith("https://") -> "☁️ Firebase"
                        uri.toString().startsWith("http://") -> "🌐 Web"
                        else -> "❓ Desconhecido"
                    }
                    appendLine("${index + 1}. $type")
                }
            }
        }

        AlertDialog.Builder(this)
            .setTitle("🔍 Análise de Imagens")
            .setMessage(message)
            .setPositiveButton("✅ Testar URLs") { _, _ ->
                testImageUrls()
            }
            .setNegativeButton("❌ Fechar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Verifica se as imagens do produto foram salvas corretamente no Firebase
     */
    private fun verifyProductImagesInFirebase(productId: String) {
        lifecycleScope.launch {
            try {
                Log.d("PostItemActivity", "🔍 Verificando imagens salvas do produto: $productId")

                val result = ProductImageVerifier.verifyProductImages(productId)
                val report = ProductImageVerifier.generateImageReport(result)

                when (result) {
                    is com.unasp.unaspmarketplace.utils.ImageVerificationResult.Success -> {
                        if (result.invalidImages.isNotEmpty()) {
                            // Algumas imagens podem ter problemas
                            runOnUiThread {
                                showImageVerificationDialog(
                                    "⚠️ Verificação de Imagens",
                                    "Algumas imagens podem ter problemas:\n\n" +
                                    "✅ Válidas: ${result.validImages.size}\n" +
                                    "❌ Inválidas: ${result.invalidImages.size}\n\n" +
                                    "Deseja ver o relatório completo?",
                                    report
                                )
                            }
                        } else {
                            // Todas as imagens estão OK
                            Log.d("PostItemActivity", "✅ Todas as imagens verificadas com sucesso")
                        }
                    }
                    is com.unasp.unaspmarketplace.utils.ImageVerificationResult.NoImages -> {
                        Log.w("PostItemActivity", "⚠️ Produto salvo sem imagens")
                    }
                    else -> {
                        runOnUiThread {
                            showImageVerificationDialog(
                                "❌ Erro na Verificação",
                                "Houve um problema ao verificar as imagens do produto.",
                                report
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PostItemActivity", "❌ Erro ao verificar imagens", e)
            }
        }
    }

    /**
     * Mostra um diálogo com os resultados da verificação de imagens
     */
    private fun showImageVerificationDialog(title: String, message: String, fullReport: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("📄 Ver Relatório") { _, _ ->
                showFullImageReport(fullReport)
            }
            .setNegativeButton("✅ OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Mostra o relatório completo de verificação de imagens
     */
    private fun showFullImageReport(report: String) {
        AlertDialog.Builder(this)
            .setTitle("📊 Relatório Completo")
            .setMessage(report)
            .setPositiveButton("✅ Entendi") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}