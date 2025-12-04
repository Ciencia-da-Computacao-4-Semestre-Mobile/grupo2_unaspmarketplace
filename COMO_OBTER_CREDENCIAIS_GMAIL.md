# 🔧 COMO OBTER CREDENCIAIS DO GMAIL PARA O SERVIDOR SMTP

## 📋 **Passo-a-Passo Completo:**

### **1. Preparar Conta Gmail:**

#### **📧 Crie ou use um Gmail:**
- Se não tem: Vá para https://gmail.com e crie uma conta
- **Recomendação**: Crie um email específico para o app
- **Exemplo**: `unaspmarketplace2024@gmail.com`

### **2. Ativar Verificação em Duas Etapas (OBRIGATÓRIO):**

#### **🔐 Acesse Configurações:**
1. Vá para: https://myaccount.google.com
2. Clique em **"Segurança"** no menu esquerdo
3. Procure por **"Verificação em duas etapas"**
4. Clique em **"Começar"**

#### **📱 Configure 2FA:**
1. Digite sua senha do Gmail
2. Adicione número de telefone
3. Receba código SMS e confirme
4. **IMPORTANTE**: Mantenha ativado!

### **3. Gerar Senha de App (MAIS IMPORTANTE):**

#### **🔑 Criar Senha de App:**
1. Ainda em https://myaccount.google.com → **Segurança**
2. Procure por **"Senhas de app"** ou **"App Passwords"**
3. Clique em **"Senhas de app"**
4. Digite sua senha do Gmail novamente

#### **📱 Configurar App:**
1. **Selecionar app**: Escolha **"Outro (nome personalizado)"**
2. **Nome**: Digite **"UNASP Marketplace"**
3. **Clique**: "Gerar"

#### **🔐 Copiar Senha Gerada:**
```
Exemplo da senha gerada:
abcd efgh ijkl mnop
```
**⚠️ IMPORTANTE**: Copie essa senha de 16 caracteres!

### **4. Configurar no Código:**

#### **✏️ Edite PasswordResetService.kt:**
```kotlin
// Linhas 29-30: Substitua pelos seus valores
private const val EMAIL_USERNAME = "unaspmarketplace2024@gmail.com"  // ← Seu Gmail
private const val EMAIL_PASSWORD = "abcd efgh ijkl mnop"              // ← Senha de app
```

## 🖼️ **Guia Visual:**

### **Tela 1 - Conta Google:**
```
┌─────────────────────────────────┐
│     🔒 Conta Google             │
│                                 │
│  👤 Dados pessoais              │
│  🔐 Segurança          ← CLIQUE │
│  🔔 Notificações               │
└─────────────────────────────────┘
```

### **Tela 2 - Segurança:**
```
┌─────────────────────────────────┐
│     🔐 Segurança                │
│                                 │
│  📱 Verificação em duas etapas  │
│      [Ativar]          ← CLIQUE │
│                                 │
│  🔑 Senhas de app               │
│      [Gerenciar]       ← CLIQUE │
└─────────────────────────────────┘
```

### **Tela 3 - Senhas de App:**
```
┌─────────────────────────────────┐
│     🔑 Senhas de app            │
│                                 │
│  Selecionar app:                │
│  [Outro ▼]             ← CLIQUE │
│                                 │
│  Nome: UNASP Marketplace        │
│  [GERAR]               ← CLIQUE │
└─────────────────────────────────┘
```

### **Tela 4 - Senha Gerada:**
```
┌─────────────────────────────────┐
│  ✅ Senha gerada com sucesso!   │
│                                 │
│  abcd efgh ijkl mnop   ← COPIE  │
│                                 │
│  [OK]                           │
└─────────────────────────────────┘
```

## 💻 **Exemplo Real de Configuração:**

### **ANTES (não funciona):**
```kotlin
private const val EMAIL_USERNAME = "marketplace.unasp@gmail.com"
private const val EMAIL_PASSWORD = "password"
```

### **DEPOIS (funciona):**
```kotlin
private const val EMAIL_USERNAME = "unaspmarketplace2024@gmail.com"
private const val EMAIL_PASSWORD = "abcd efgh ijkl mnop"
```

## ⚠️ **AVISOS IMPORTANTES:**

### **❌ NÃO USE:**
- ❌ Senha normal do Gmail
- ❌ Email sem verificação em duas etapas
- ❌ Senha de app antiga/expirada

### **✅ USE SEMPRE:**
- ✅ Senha de app de 16 caracteres
- ✅ Verificação em duas etapas ativada
- ✅ Email válido e ativo

## 🧪 **Teste Rápido:**

### **Após configurar:**
1. **Compile**: Projeto sem erros
2. **Execute**: App no celular/emulador
3. **Teste**: "Esqueci minha senha"
4. **Digite**: Qualquer email válido
5. **Aguarde**: 1-5 minutos
6. **Verifique**: Caixa de entrada + spam

## 🔍 **Troubleshooting:**

### **Erro "Authentication failed":**
- ✅ Verifique se usa senha de app (não senha normal)
- ✅ Confirme que 2FA está ativado
- ✅ Gere nova senha de app

### **Erro "Connection timeout":**
- ✅ Verifique internet
- ✅ Teste em rede diferente
- ✅ Verifique firewall

### **Email não chega:**
- ✅ Verifique pasta de spam
- ✅ Aguarde até 5 minutos
- ✅ Teste com email diferente

---

**🎯 RESUMO**: Crie Gmail → Ative 2FA → Gere senha de app → Configure no código → Teste!

Siga exatamente esses passos e o sistema funcionará perfeitamente! 📧✅
