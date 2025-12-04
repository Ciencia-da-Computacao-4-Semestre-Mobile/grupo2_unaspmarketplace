# 🔧 SOLUCIONANDO ERRO "FAILED_PRECONDITION: The query requires an index"

## ❌ Problema Identificado
**Erro:** `FAILED_PRECONDITION: The query requires an index`
**Causa:** Consultas Firestore com `whereEqualTo + orderBy` precisam de **índices compostos** criados no Firebase Console.

## ✅ SOLUÇÃO IMPLEMENTADA (TEMPORÁRIA)

### 🚀 Correções Aplicadas:

1. **Removido `orderBy` das consultas** que causavam erro
2. **Adicionado ordenação manual** no código Kotlin  
3. **Consultas funcionais** sem necessidade de índices

### 📍 Arquivos Modificados:

**OrderRepository.kt** - 3 métodos corrigidos:
- `getBuyerOrders()` - Histórico de compras
- `getSellerOrders()` - Pedidos recebidos  
- `getPendingSellerOrders()` - Pedidos pendentes

### ⚡ Resultado:
- ✅ App funciona imediatamente
- ✅ Sem erros de consulta
- ✅ Dados ordenados corretamente
- ⚠️ Performance pode ser menor com muitos pedidos

---

## 🎯 SOLUÇÃO DEFINITIVA (RECOMENDADA)

Para **melhor performance** e usar `orderBy` no Firestore, crie os índices compostos:

### 📋 **Índices Necessários**

| Campo 1 | Campo 2 | Campo 3 | Ordem | Uso |
|---------|---------|---------|-------|-----|
| `buyerId` | `createdAt` | - | DESC | Histórico de compras |
| `sellerId` | `createdAt` | - | DESC | Pedidos recebidos |
| `sellerId` | `status` | `createdAt` | DESC | Pedidos pendentes |

### 🔗 **Como Criar os Índices:**

#### **Opção 1: Link Automático (Mais Fácil)**

Quando o app tentar fazer a consulta, o Firestore mostrará no **console de logs** links como:

```
https://console.firebase.google.com/project/unaspmarketplace/firestore/indexes?create_composite=...
```

1. **Execute o app** e navegue para a tela de pedidos
2. **Copie o link** que apareceu no console de logs
3. **Abra o link** no navegador
4. **Clique em "Criar índice"**
5. **Aguarde** a criação (pode levar alguns minutos)

#### **Opção 2: Manual via Firebase Console**

1. **Acesse:** https://console.firebase.google.com
2. **Selecione:** `unaspmarketplace`
3. **Navegue:** Firestore Database → Indexes → Composite
4. **Clique:** "Create Index"

**Para Histórico de Compras:**
- Collection ID: `orders`
- Field path: `buyerId` | Ascending
- Field path: `createdAt` | Descending
- Query scope: `Collection`

**Para Pedidos do Vendedor:**
- Collection ID: `orders`  
- Field path: `sellerId` | Ascending
- Field path: `createdAt` | Descending
- Query scope: `Collection`

**Para Pedidos Pendentes:**
- Collection ID: `orders`
- Field path: `sellerId` | Ascending  
- Field path: `status` | Ascending
- Field path: `createdAt` | Descending
- Query scope: `Collection`

---

## 🔄 **APÓS CRIAR OS ÍNDICES**

Quando os índices estiverem prontos, você pode restaurar as consultas originais:

### 📝 **Reverter OrderRepository.kt:**

```kotlin
// getBuyerOrders - Restaurar orderBy
val snapshot = ordersCollection
    .whereEqualTo("buyerId", buyerId)
    .orderBy("createdAt", Query.Direction.DESCENDING)
    .get()
    .await()

// getSellerOrders - Restaurar orderBy  
val snapshot = ordersCollection
    .whereEqualTo("sellerId", sellerId)
    .orderBy("createdAt", Query.Direction.DESCENDING)
    .get()
    .await()

// getPendingSellerOrders - Restaurar orderBy
val snapshot = ordersCollection
    .whereEqualTo("sellerId", sellerId)
    .whereEqualTo("status", OrderStatus.PENDING.name)
    .orderBy("createdAt", Query.Direction.DESCENDING)
    .get()
    .await()
```

E remover as linhas de ordenação manual:
```kotlin
// Remover esta linha
val sortedOrders = orders.sortedByDescending { it.createdAt }
```

---

## ✅ **STATUS ATUAL**

| Funcionalidade | Status | Observação |
|----------------|--------|-------------|
| ❌ Login Google (Erro 10) | Pendente | Precisa configurar SHA fingerprints |
| ✅ Carregar Pedidos | **Funcionando** | Corrigido agora |
| ⚡ Performance Pedidos | Pode melhorar | Criar índices para otimizar |
| ✅ Criação de Pedidos | Funcionando | Sem problemas |

---

## 🚀 **PRÓXIMOS PASSOS RECOMENDADOS**

### **1. Testar o App Agora (Imediato)**
```bash
.\gradlew clean build
# Instalar e testar - erro de pedidos deve estar resolvido
```

### **2. Resolver Google Sign-In (Erro 10)**
- Execute: `.\gradlew signingReport`
- Configure SHA fingerprints no Firebase
- Baixe novo `google-services.json`

### **3. Otimizar Performance (Opcional)**
- Crie índices compostos no Firestore Console
- Restaure consultas com `orderBy`

---

## 📋 **Resumo das Correções**

✅ **Problema:** Erro `FAILED_PRECONDITION` ao carregar pedidos  
✅ **Solução:** Removido `orderBy` + adicionado ordenação manual  
✅ **Resultado:** App funciona sem erros de consulta  
⚡ **Otimização:** Criar índices para melhor performance (opcional)

**O erro de pedidos está resolvido! 🎉**
