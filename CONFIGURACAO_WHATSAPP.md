# 📱 CONFIGURAÇÃO DO WHATSAPP - UNASP STORE

## 🚨 IMPORTANTE - CONFIGURAR ANTES DE USAR

### 📞 **Como Configurar o Número da UNASP Store:**

1. **Abra o arquivo:** `app/src/main/java/com/unasp/unaspmarketplace/utils/Constants.kt`

2. **Localize a linha:**
```kotlin
const val UNASP_STORE_PHONE = "5511999999999"
```

3. **Substitua pelo número real da UNASP Store:**
```kotlin
const val UNASP_STORE_PHONE = "55XXXXXXXXXXX"  // Seu número aqui
```

### 📋 **Formato do Número:**

**Formato correto:** `Código do País + DDD + Número`
- **Sem espaços, traços ou parênteses**
- **Apenas números**

**Exemplos:**
- **(11) 99999-9999** → `"5511999999999"`
- **(47) 88888-8888** → `"5547888888888"`
- **(21) 77777-7777** → `"5521777777777"`

### ✅ **Teste da Configuração:**

1. Adicione alguns produtos ao carrinho
2. Vá para "Finalizar Compra"
3. Preencha seu nome
4. Escolha uma forma de pagamento
5. Clique em "Gerar Pedido"
6. Verifique se abre o WhatsApp com o número correto

### 🔧 **Se Precisar Alterar Outras Configurações:**

**Nome da loja:**
```kotlin
const val UNASP_STORE_NAME = "UNASP Store"
```

**Mensagens:**
```kotlin
const val WHATSAPP_NOT_FOUND = "WhatsApp não está instalado. Será aberto no navegador."
const val ORDER_SUCCESS = "Pedido enviado com sucesso!"
```

---

## 🚀 **Depois de Configurar:**

O sistema automaticamente:
- ✅ Gerará IDs únicos para cada pedido
- ✅ Formatará a mensagem profissionalmente
- ✅ Abrirá o WhatsApp automaticamente
- ✅ Direcionará para o número da UNASP Store
- ✅ Incluirá todos os detalhes do pedido

**⚠️ LEMBRE-SE:** Teste sempre após configurar para garantir que está funcionando corretamente!
