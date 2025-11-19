# 📧 EXEMPLO PRÁTICO: CONFIGURAÇÃO CORRETA

## 🎯 **Como Suas Credenciais Devem Ficar:**

### **📝 Arquivo: PasswordResetService.kt (linhas 29-30)**

#### **ANTES (não funciona):**
```kotlin
private const val EMAIL_USERNAME = "marketplace.unasp@gmail.com"
private const val EMAIL_PASSWORD = "password"
```

#### **DEPOIS (funcionará):**
```kotlin
private const val EMAIL_USERNAME = "unaspmarketplace2024@gmail.com"
private const val EMAIL_PASSWORD = "abcd efgh ijkl mnop"
```

## 🔑 **Onde Obter Esses Valores:**

### **1. EMAIL_USERNAME:**
- **O que é**: Seu email Gmail real
- **Onde obter**: Gmail que você criou ou usa
- **Exemplos válidos**:
  - `unaspmarketplace2024@gmail.com`
  - `marketplace.projeto@gmail.com`
  - `seunome.projeto@gmail.com`

### **2. EMAIL_PASSWORD:**
- **O que é**: Senha de app do Gmail (16 caracteres)
- **Onde obter**: Google Account → Segurança → Senhas de app
- **Formato**: `abcd efgh ijkl mnop` (4 grupos de 4 letras)
- **⚠️ NÃO é a senha normal do Gmail!**

## 🚀 **Processo Completo:**

### **Passo 1: Criar/Usar Gmail**
```
1. Vá para https://gmail.com
2. Crie conta: unaspmarketplace2024@gmail.com
3. Confirme email e configure
```

### **Passo 2: Ativar 2FA**
```
1. Vá para https://myaccount.google.com
2. Segurança → Verificação em duas etapas
3. Adicione telefone e ative
```

### **Passo 3: Gerar Senha de App**
```
1. Ainda em Segurança → Senhas de app
2. Selecione "Outro" → "UNASP Marketplace"
3. Copie senha: abcd efgh ijkl mnop
```

### **Passo 4: Configurar Código**
```kotlin
// Substitua no PasswordResetService.kt:
private const val EMAIL_USERNAME = "unaspmarketplace2024@gmail.com"
private const val EMAIL_PASSWORD = "abcd efgh ijkl mnop"
```

### **Passo 5: Testar**
```
1. Compile projeto
2. Execute app
3. Teste "Esqueci minha senha"
4. Verifique email chegando
```

## 💡 **Dicas Importantes:**

### **✅ FAÇA:**
- Use email dedicado para o projeto
- Mantenha 2FA sempre ativado
- Guarde senha de app em local seguro
- Teste com emails diferentes

### **❌ NÃO FAÇA:**
- Usar senha normal do Gmail no código
- Desativar verificação em duas etapas
- Compartilhar credenciais publicamente
- Commitar credenciais para Git

## 🧪 **Teste Rápido:**

Após configurar, execute:
```cmd
configurar_gmail_smtp.bat
```

Ou teste manualmente:
1. Execute app
2. "Esqueci minha senha"
3. Digite qualquer email
4. Verifique caixa de entrada + spam

## 📋 **Checklist Final:**

- [ ] Gmail criado/escolhido
- [ ] Verificação em duas etapas ativada  
- [ ] Senha de app gerada (16 chars)
- [ ] EMAIL_USERNAME configurado no código
- [ ] EMAIL_PASSWORD configurado no código
- [ ] Projeto compilando sem erros
- [ ] Email teste enviado e recebido

---

**🎯 Execute o script `configurar_gmail_smtp.bat` que criamos para te guiar passo-a-passo!**

Ele vai te ajudar a obter todas as credenciais necessárias e configurar corretamente o sistema. 📧✅
