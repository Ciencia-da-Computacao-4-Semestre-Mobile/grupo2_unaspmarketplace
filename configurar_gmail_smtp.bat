@echo off
echo === GUIA: OBTER CREDENCIAIS GMAIL PARA SMTP ===
echo Data: %date% %time%
echo.

echo 🎯 OBJETIVO: Configurar email real para envio de tokens
echo.

echo === PASSO 1: PREPARAR GMAIL ===
echo.
echo 1.1 📧 Crie ou use um Gmail existente:
echo     • Acesse: https://gmail.com
echo     • Recomendação: Criar email específico para o app
echo     • Exemplo: unaspmarketplace2024@gmail.com
echo.
echo 1.2 ✅ Email criado/escolhido?
set /p email_ready="Digite S para continuar: "
if /i "%email_ready%" neq "S" (
    echo ❌ Configure o Gmail primeiro!
    pause
    exit
)

echo.
echo === PASSO 2: ATIVAR VERIFICAÇÃO EM DUAS ETAPAS ===
echo.
echo 2.1 🔐 Acesse configurações de segurança:
echo     • Vá para: https://myaccount.google.com
echo     • Clique em "Segurança"
echo.
echo 2.2 📱 Ative verificação em duas etapas:
echo     • Procure "Verificação em duas etapas"
echo     • Clique "Começar"
echo     • Adicione número de telefone
echo     • Confirme com código SMS
echo.
echo 2.3 ✅ Verificação em duas etapas ativada?
set /p two_factor_ready="Digite S para continuar: "
if /i "%two_factor_ready%" neq "S" (
    echo ❌ Ative a verificação em duas etapas primeiro!
    echo 💡 É obrigatório para senhas de app!
    pause
    exit
)

echo.
echo === PASSO 3: GERAR SENHA DE APP ===
echo.
echo 3.1 🔑 Acessar senhas de app:
echo     • Ainda em https://myaccount.google.com
echo     • Na seção "Segurança"
echo     • Procure "Senhas de app" ou "App Passwords"
echo     • Clique em "Senhas de app"
echo.
echo 3.2 📱 Gerar nova senha:
echo     • Digite sua senha do Gmail
echo     • Selecionar app: "Outro (nome personalizado)"
echo     • Nome: "UNASP Marketplace"
echo     • Clique "GERAR"
echo.
echo 3.3 🔐 Copie a senha gerada:
echo     • Será algo como: "abcd efgh ijkl mnop"
echo     • São 16 caracteres (4 grupos de 4)
echo     • COPIE essa senha!
echo.
echo 3.4 ✅ Senha de app gerada e copiada?
set /p app_password_ready="Digite S para continuar: "
if /i "%app_password_ready%" neq "S" (
    echo ❌ Gere a senha de app primeiro!
    pause
    exit
)

echo.
echo === PASSO 4: CONFIGURAR NO CÓDIGO ===
echo.
echo 4.1 📝 Valores a configurar:
echo.
set /p user_email="Digite seu email Gmail: "
set /p user_password="Digite a senha de app (16 chars): "

echo.
echo 4.2 ✅ Verificação dos dados:
echo     EMAIL_USERNAME: "%user_email%"
echo     EMAIL_PASSWORD: "%user_password%"
echo.

if "%user_email%"=="" (
    echo ❌ Email não pode estar vazio!
    pause
    exit
)

if "%user_password%"=="" (
    echo ❌ Senha de app não pode estar vazia!
    pause
    exit
)

echo 4.3 📁 Abrindo arquivo para edição...
echo.
echo ⚠️  EDITE AS LINHAS 29-30 em PasswordResetService.kt:
echo.
echo SUBSTITUA:
echo private const val EMAIL_USERNAME = "marketplace.unasp@gmail.com"
echo private const val EMAIL_PASSWORD = "password"
echo.
echo POR:
echo private const val EMAIL_USERNAME = "%user_email%"
echo private const val EMAIL_PASSWORD = "%user_password%"
echo.

echo 4.4 📝 Abrir arquivo para edição?
set /p open_file="Digite S para abrir o arquivo: "
if /i "%open_file%"=="S" (
    start notepad "app\src\main\java\com\unasp\unaspmarketplace\services\PasswordResetService.kt"
    echo ✅ Arquivo aberto no Notepad
    echo 📝 Edite as linhas 29-30 com os valores acima
) else (
    echo 💡 Edite manualmente o arquivo PasswordResetService.kt
)

echo.
echo === PASSO 5: TESTAR SISTEMA ===
echo.
echo 5.1 🔧 Após editar o arquivo:
echo     1. Salve o arquivo
echo     2. Compile o projeto
echo     3. Execute o app
echo     4. Teste "Esqueci minha senha"
echo.

set /p test_ready="Arquivo editado e pronto para testar? (S/N): "
if /i "%test_ready%"=="S" (
    echo.
    echo 🧪 TESTANDO COMPILAÇÃO...
    cd /d "%~dp0"
    gradlew.bat clean compileDebugKotlin

    if %errorlevel% equ 0 (
        echo ✅ COMPILAÇÃO BEM-SUCEDIDA!
        echo.
        echo 🎉 SISTEMA CONFIGURADO COM SUCESSO!
        echo.
        echo === PRÓXIMOS PASSOS ===
        echo 1. Execute o app
        echo 2. Teste "Esqueci minha senha"
        echo 3. Use qualquer email válido
        echo 4. Verifique a caixa de entrada
        echo 5. Use o token recebido
        echo.
        echo 📧 O sistema agora enviará emails reais!
    ) else (
        echo ❌ ERRO NA COMPILAÇÃO
        echo 💡 Verifique se editou corretamente o arquivo
        gradlew.bat compileDebugKotlin 2>&1 | findstr /i "error"
    )
) else (
    echo 💡 Edite o arquivo primeiro, depois execute este script novamente
)

echo.
echo === RESUMO DO QUE VOCÊ PRECISA ===
echo 1. ✅ Gmail configurado
echo 2. ✅ Verificação em duas etapas ativada
echo 3. ✅ Senha de app gerada
echo 4. ⏳ Arquivo PasswordResetService.kt editado
echo 5. ⏳ Projeto compilado
echo 6. ⏳ Sistema testado
echo.
echo 📖 Veja detalhes em: COMO_OBTER_CREDENCIAIS_GMAIL.md
echo.

pause
