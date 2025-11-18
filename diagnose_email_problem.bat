@echo off
echo === DIAGNÓSTICO: POR QUE O EMAIL NÃO ESTÁ CHEGANDO? ===
echo Data: %date% %time%
echo.

cd /d "I:\AndroidStudio\grupo2_unaspmarketplace"

echo 1. Verificando configuração de credenciais...
echo.

findstr /n "seu.email@gmail.com" app\src\main\java\com\unasp\unaspmarketplace\services\PasswordResetService.kt >nul
if %errorlevel% equ 0 (
    echo ❌ PROBLEMA ENCONTRADO: Credenciais não configuradas!
    echo.
    echo 🔧 SOLUÇÃO NECESSÁRIA:
    echo 1. Edite: app\src\main\java\com\unasp\unaspmarketplace\services\PasswordResetService.kt
    echo 2. Linha ~23: Substitua "seu.email@gmail.com" pelo seu email real
    echo 3. Linha ~24: Substitua "sua_senha_de_app" pela senha de app do Gmail
    echo.
    echo 📧 COMO OBTER SENHA DE APP DO GMAIL:
    echo 1. Vá para: https://myaccount.google.com
    echo 2. Segurança → Verificação em duas etapas (ative se não tiver)
    echo 3. Senhas de app → Criar → "UNASP Marketplace"
    echo 4. Use a senha de 16 caracteres gerada
    echo.
    echo 💡 ENQUANTO NÃO CONFIGURAR:
    echo - O sistema vai gerar tokens mas não enviar emails
    echo - Verifique os logs para ver o token gerado
    echo - Procure por "TOKEN DE RECUPERAÇÃO GERADO" no Logcat
    echo.
) else (
    echo ✅ Credenciais parecem estar configuradas
    echo.
    echo 🔍 OUTROS POSSÍVEIS PROBLEMAS:
    echo 1. Senha de app incorreta
    echo 2. Verificação em duas etapas desativada
    echo 3. Email na pasta de spam
    echo 4. Problemas de conectividade
    echo.
)

echo 2. Verificando se função de debugging está implementada...
findstr /n "storeTokenForDebugging" app\src\main\java\com\unasp\unaspmarketplace\services\PasswordResetService.kt >nul
if %errorlevel% equ 0 (
    echo ✅ Sistema de debugging implementado
) else (
    echo ❌ Sistema de debugging não encontrado
)

echo.
echo 3. Compilando projeto...
gradlew.bat clean compileDebugKotlin

if %errorlevel% equ 0 (
    echo.
    echo ✅ COMPILAÇÃO BEM-SUCEDIDA

    echo.
    echo 4. Testando build...
    gradlew.bat assembleDebug

    if %errorlevel% equ 0 (
        echo.
        echo 🎉 BUILD BEM-SUCEDIDO! APK gerado.
        echo.
        echo === COMO TESTAR O SISTEMA ===
        echo.
        echo 📱 TESTE NO APLICATIVO:
        echo 1. Execute o app
        echo 2. Tente "Esqueci minha senha"
        echo 3. Digite um email
        echo 4. Observe o Logcat no Android Studio
        echo.
        echo 🔍 O QUE PROCURAR NO LOGCAT:
        echo.
        echo SE CREDENCIAIS NÃO CONFIGURADAS:
        echo   🟡 "CREDENCIAIS NÃO CONFIGURADAS!"
        echo   🟡 "TOKEN DE RECUPERAÇÃO GERADO"
        echo   🟡 Token: 12345 (use este na tela de verificação)
        echo.
        echo SE CREDENCIAIS CONFIGURADAS MAS COM ERRO:
        echo   ❌ "Erro de SMTP"
        echo   ❌ "Authentication failed" (senha incorreta)
        echo   ❌ "Connection" (problema de rede)
        echo.
        echo SE TUDO OK:
        echo   ✅ "Email de recuperação enviado com sucesso"
        echo.
        echo === PRÓXIMOS PASSOS ===
        echo.
        if errorlevel 0 (
            findstr /n "seu.email@gmail.com" app\src\main\java\com\unasp\unaspmarketplace\services\PasswordResetService.kt >nul
            if !errorlevel! equ 0 (
                echo 🔧 CONFIGURE AS CREDENCIAIS:
                echo 1. Abra: PasswordResetService.kt
                echo 2. Configure EMAIL_USERNAME com seu email real
                echo 3. Configure EMAIL_PASSWORD com senha de app do Gmail
                echo 4. Teste novamente
                echo.
                echo 📖 Veja instruções detalhadas em: CONFIGURACAO_EMAIL_GMAIL.md
            ) else (
                echo ✅ CREDENCIAIS CONFIGURADAS - TESTE O SISTEMA:
                echo 1. Execute o app
                echo 2. Use "Esqueci minha senha"
                echo 3. Verifique sua caixa de entrada + spam
                echo 4. Se não chegar, verifique logs de erro
            )
        )

    ) else (
        echo ❌ Erro no build
        gradlew.bat assembleDebug 2>&1 | findstr /i "error\|failed"
    )

) else (
    echo ❌ Erro na compilação
    gradlew.bat compileDebugKotlin 2>&1 | findstr /i "error\|unresolved"
)

echo.
echo === RESUMO DO DIAGNÓSTICO ===
echo.
echo 🚨 MOTIVOS PRINCIPAIS PARA EMAIL NÃO CHEGAR:
echo 1. ❌ Credenciais não configuradas (mais comum)
echo 2. ❌ Senha de app incorreta
echo 3. ❌ Verificação em duas etapas desativada
echo 4. ❌ Email indo para pasta de spam
echo 5. ❌ Problemas de firewall/rede
echo.
echo 🔧 SOLUÇÃO RÁPIDA:
echo Configure as credenciais em PasswordResetService.kt
echo Obtenha senha de app do Gmail
echo Teste e verifique logs
echo.

pause
