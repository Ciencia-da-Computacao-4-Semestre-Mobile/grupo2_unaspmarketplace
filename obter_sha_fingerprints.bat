@echo off
echo ========================================
echo  OBTENDO SHA-1 E SHA-256 FINGERPRINTS
echo ========================================
echo.

echo [1/4] Localizando keytool...
echo ----------------------------------------

REM Procurar keytool em locais comuns
set KEYTOOL=""
if exist "%JAVA_HOME%\bin\keytool.exe" (
    set KEYTOOL="%JAVA_HOME%\bin\keytool.exe"
    echo Keytool encontrado em JAVA_HOME: %JAVA_HOME%\bin\keytool.exe
) else if exist "C:\Program Files\Java\jdk*\bin\keytool.exe" (
    for /d %%i in ("C:\Program Files\Java\jdk*") do (
        if exist "%%i\bin\keytool.exe" (
            set KEYTOOL="%%i\bin\keytool.exe"
            echo Keytool encontrado em: %%i\bin\keytool.exe
            goto :found
        )
    )
) else if exist "C:\Program Files\Android\Android Studio\jre\bin\keytool.exe" (
    set KEYTOOL="C:\Program Files\Android\Android Studio\jre\bin\keytool.exe"
    echo Keytool encontrado no Android Studio: C:\Program Files\Android\Android Studio\jre\bin\keytool.exe
) else if exist "C:\Users\%USERNAME%\AppData\Local\Android\Sdk\build-tools\*\keytool.exe" (
    for /d %%i in ("C:\Users\%USERNAME%\AppData\Local\Android\Sdk\build-tools\*") do (
        if exist "%%i\keytool.exe" (
            set KEYTOOL="%%i\keytool.exe"
            echo Keytool encontrado no Android SDK: %%i\keytool.exe
            goto :found
        )
    )
)

:found
if %KEYTOOL%=="" (
    echo.
    echo ERRO: Keytool nao encontrado!
    echo.
    echo SOLUCOES ALTERNATIVAS:
    echo ----------------------------------------
    echo 1. Instale o Java JDK e adicione ao PATH
    echo 2. Use Android Studio: Build ^> Generate Signed Bundle/APK ^> View SHA
    echo 3. Use o comando manual abaixo:
    echo.
    echo COMANDO MANUAL:
    echo "C:\Program Files\Java\jdk-XX\bin\keytool.exe" -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
    echo.
    echo Ou use o Gradle:
    echo .\gradlew signingReport
    echo.
    goto :end
)

echo [2/4] Obtendo SHA do Debug Keystore...
echo ----------------------------------------
echo.

REM Caminho padrão do debug keystore no Windows
set DEBUG_KEYSTORE=%USERPROFILE%\.android\debug.keystore

if exist "%DEBUG_KEYSTORE%" (
    echo Debug Keystore encontrado em: %DEBUG_KEYSTORE%
    echo.
    echo SHA-1 E SHA-256 FINGERPRINTS (DEBUG):
    echo ========================================
    %KEYTOOL% -list -v -keystore "%DEBUG_KEYSTORE%" -alias androiddebugkey -storepass android -keypass android | findstr /i "SHA1 SHA256"
    echo ========================================
    echo.
) else (
    echo ERRO: Debug keystore nao encontrado em %DEBUG_KEYSTORE%
    echo.
    echo CRIANDO DEBUG KEYSTORE...
    echo ----------------------------------------
    %KEYTOOL% -genkey -v -keystore "%DEBUG_KEYSTORE%" -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
    echo.
    echo Agora execute o script novamente para obter os SHA fingerprints.
    echo.
    goto :end
)

echo.
echo [3/4] Usando Gradle (Alternativo):
echo ----------------------------------------
echo Voce tambem pode usar o comando Gradle:
echo .\gradlew signingReport
echo.

echo.
echo [4/4] Instrucoes para adicionar no Firebase Console:
echo.
echo [4/4] Instrucoes para adicionar no Firebase Console:
echo ----------------------------------------
echo 1. Acesse: https://console.firebase.google.com
echo 2. Selecione o projeto: unaspmarketplace
echo 3. Va em: Configuracoes do Projeto (icone de engrenagem)
echo 4. Aba: Geral
echo 5. Role ate "Seus apps"
echo 6. Clique no app Android: com.unasp.unaspmarketplace
echo 7. Clique em "Adicionar impressao digital"
echo 8. Cole o SHA-1 (copie da linha acima que comeca com SHA1:)
echo 9. Clique em "Salvar"
echo 10. Repita para SHA-256
echo.

echo.
echo Informacoes do Projeto:
echo ----------------------------------------
echo Package Name: com.unasp.unaspmarketplace
echo Project ID: unaspmarketplace
echo.

echo.
echo ========================================
echo  PRÓXIMOS PASSOS
echo ========================================
echo.
echo 1. Copie os SHA-1 e SHA-256 acima
echo 2. Adicione no Firebase Console (instrucoes acima)
echo 3. Baixe o novo google-services.json
echo 4. Substitua o arquivo em: app\google-services.json
echo 5. Rebuild o projeto: .\gradlew clean build
echo 6. Reinstale o app no dispositivo
echo.

:end
pause

