# 🔧 SOLUCIONANDO ERRO 10 DO GOOGLE SIGN-IN

## ❌ Problema: Erro 10 do Google Sign-In
O erro 10 indica que os SHA-1/SHA-256 fingerprints não estão configurados no Firebase Console ou estão incorretos.

## ✅ SOLUÇÕES (em ordem de facilidade)

### 🎯 **SOLUÇÃO 1: Usando Gradle (MAIS FÁCIL)**

Execute este comando no terminal (dentro da pasta do projeto):

```bash
.\gradlew signingReport
```

**Resultado esperado:**
```
Variant: debug
Config: debug
Store: C:\Users\[SEU_USUARIO]\.android\debug.keystore
Alias: AndroidDebugKey
MD5: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
SHA1: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
SHA-256: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
```

### 🎯 **SOLUÇÃO 2: Usando Android Studio**

1. Abra o projeto no Android Studio
2. Menu: **Build** → **Generate Signed Bundle / APK**
3. Escolha **APK**
4. Clique em **Next**
5. Na tela de keystore, clique em **Choose existing...**
6. Navegue para: `C:\Users\[SEU_USUARIO]\.android\debug.keystore`
7. Password: `android`
8. Key alias: `androiddebugkey` 
9. Key password: `android`
10. Clique **Next**
11. **IMPORTANTE:** Antes de continuar, clique no ícone **🔍 (View Details)**
12. Copie os valores SHA1 e SHA256

### 🎯 **SOLUÇÃO 3: Instalando Java JDK**

Se as soluções acima não funcionarem:

1. **Baixe o Java JDK:**
   - Acesse: https://adoptium.net/
   - Baixe OpenJDK 17 ou superior
   - Instale normalmente

2. **Execute o comando keytool:**
```bash
"C:\Program Files\Eclipse Adoptium\jdk-17.0.x.x-hotspot\bin\keytool.exe" -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

## 📋 **CONFIGURAÇÃO NO FIREBASE CONSOLE**

1. **Acesse:** https://console.firebase.google.com
2. **Selecione o projeto:** `unaspmarketplace`
3. **Clique no ícone ⚙️ (Configurações do projeto)**
4. **Aba:** Geral
5. **Role até:** "Seus apps"
6. **Clique no app Android:** `com.unasp.unaspmarketplace`
7. **Na seção "Impressões digitais do certificado SHA":**
   - Clique em **"Adicionar impressão digital"**
   - Cole o **SHA-1** (exemplo: `12:34:56:78:90:AB:CD:EF:...`)
   - Clique **"Salvar"**
8. **Repita para SHA-256** (se necessário)

## 🔄 **APÓS ADICIONAR OS SHA FINGERPRINTS**

1. **Baixe o novo `google-services.json`:**
   - No Firebase Console, clique no app Android
   - Clique em **"Baixar google-services.json"**

2. **Substitua o arquivo:**
   - Salve o novo arquivo em: `app/google-services.json`
   - Substitua o arquivo existente

3. **Rebuild o projeto:**
```bash
.\gradlew clean build
```

4. **Desinstale completamente o app do dispositivo**

5. **Reinstale o app via Android Studio**

## 🎯 **INFORMAÇÕES DO SEU PROJETO**

| Campo | Valor |
|-------|-------|
| Package Name | `com.unasp.unaspmarketplace` |
| Project ID | `unaspmarketplace` |
| Current SHA (no google-services.json) | `309aa317423b9cf10037d5b4654c8f4642b07086` |

## 🔍 **VERIFICAÇÃO**

Para verificar se deu certo:

1. Execute o app
2. Tente fazer login com Google
3. Se ainda der erro 10, verifique se:
   - O SHA foi adicionado corretamente no Firebase
   - O novo `google-services.json` foi baixado e substituído
   - O app foi completamente desinstalado e reinstalado

## ⚠️ **TROUBLESHOOTING**

### Erro: "Debug keystore not found"
Se o debug keystore não existir, crie um novo:
```bash
keytool -genkey -v -keystore "%USERPROFILE%\.android\debug.keystore" -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
```

### Gradle não funciona
- Certifique-se de estar na pasta raiz do projeto
- Verifique se o `gradlew` existe e tem permissões de execução
- Tente: `./gradlew.bat signingReport` (Windows)

### SHA fingerprints diferentes
- Use sempre o debug keystore para desenvolvimento
- Para produção, use o keystore de release
- Certifique-se de usar o mesmo keystore que assinou o APK

---

## 🚀 **RESUMO DOS COMANDOS**

**Para obter SHA fingerprints:**
```bash
.\gradlew signingReport
```

**Para rebuild:**
```bash
.\gradlew clean build
```

**Para verificar keystore:**
```bash
dir "%USERPROFILE%\.android\debug.keystore"
```

---

**Prioridade: Execute `.\gradlew signingReport` primeiro!**
