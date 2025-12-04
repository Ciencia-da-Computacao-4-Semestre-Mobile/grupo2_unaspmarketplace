# Debug - Criação de Pedidos

## Problemas Identificados

### 1. Estrutura dos Dados

A estrutura parece estar correta:
- **Order.kt**: Modelo bem definido com todos os campos necessários
- **OrderItem.kt**: Modelo correto
- **OrderRepository.kt**: Implementação usando Firebase Firestore
- **PaymentActivity.kt**: Lógica de criação de pedidos

### 2. Possíveis Causas do Erro

#### A. Verificar se o usuário está logado
```kotlin
val currentUser = UserUtils.getCurrentUser()
val buyerId = currentUser?.id ?: ""
```

#### B. Verificar se o carrinho tem itens
```kotlin
val cartItems = CartManager.getCartItems()
if (cartItems.isEmpty()) {
    Toast.makeText(this, "Carrinho vazio!", Toast.LENGTH_SHORT).show()
    return
}
```

#### C. Verificar se o produto tem sellerId
```kotlin
val sellerId = cartItems[0].product.sellerId
```

### 3. Fluxo de Debug Necessário

1. Adicionar logs detalhados na criação do pedido
2. Verificar se o Firebase está configurado corretamente
3. Verificar se as permissões do Firestore estão corretas
4. Validar se todos os campos obrigatórios estão sendo preenchidos

