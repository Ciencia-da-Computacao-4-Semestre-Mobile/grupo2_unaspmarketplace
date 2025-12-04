#!/usr/bin/env pwsh
# PowerShell script para obter SHA fingerprints para Google Sign-In

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  OBTENDO SHA-1 E SHA-256 FINGERPRINTS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "[1/4] Localizando keytool..." -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

$keytoolPath = $null
$possiblePaths = @(
    "${env:JAVA_HOME}\bin\keytool.exe",
    "C:\Program Files\Java\jdk*\bin\keytool.exe",
    "C:\Program Files\Eclipse Adoptium\jdk*\bin\keytool.exe",
    "C:\Program Files\Android\Android Studio\jre\bin\keytool.exe",
    "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe",
    "${env:LOCALAPPDATA}\Android\Sdk\build-tools\*\keytool.exe"
)

foreach ($path in $possiblePaths) {
    if ($path -like "*\*\*") {
        # Path com wildcard, procurar
        $expandedPaths = Get-ChildItem -Path (Split-Path $path) -Filter (Split-Path $path -Leaf) -Recurse -ErrorAction SilentlyContinue | Where-Object { $_.Name -eq "keytool.exe" }
        if ($expandedPaths) {
            $keytoolPath = $expandedPaths[0].FullName
            Write-Host "Keytool encontrado em: $keytoolPath" -ForegroundColor Green
            break
        }
    } elseif (Test-Path $path) {
        $keytoolPath = $path
        Write-Host "Keytool encontrado em: $keytoolPath" -ForegroundColor Green
        break
    }
}

if (-not $keytoolPath) {
    Write-Host ""
    Write-Host "ERRO: Keytool não encontrado!" -ForegroundColor Red
    Write-Host ""
    Write-Host "SOLUÇÕES ALTERNATIVAS:" -ForegroundColor Yellow
    Write-Host "----------------------------------------" -ForegroundColor Yellow
    Write-Host "1. Use o comando Gradle: .\gradlew signingReport" -ForegroundColor White
    Write-Host "2. Instale o Java JDK" -ForegroundColor White
    Write-Host "3. Use Android Studio: Build > Generate Signed Bundle/APK > View SHA" -ForegroundColor White
    Write-Host ""
    Write-Host "COMANDO GRADLE (Execute este comando):" -ForegroundColor Cyan
    Write-Host ".\gradlew signingReport" -ForegroundColor White
    Write-Host ""
    exit 1
}

Write-Host ""
Write-Host "[2/4] Verificando debug keystore..." -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

$debugKeystore = "$env:USERPROFILE\.android\debug.keystore"

if (Test-Path $debugKeystore) {
    Write-Host "Debug Keystore encontrado em: $debugKeystore" -ForegroundColor Green
    Write-Host ""
    Write-Host "SHA-1 E SHA-256 FINGERPRINTS (DEBUG):" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan

    # Executar keytool e capturar output
    try {
        $output = & $keytoolPath -list -v -keystore $debugKeystore -alias androiddebugkey -storepass android -keypass android 2>&1
        $shaLines = $output | Where-Object { $_ -match "SHA1|SHA256" }

        foreach ($line in $shaLines) {
            if ($line -match "SHA1") {
                Write-Host "SHA1: " -NoNewline -ForegroundColor Green
                Write-Host ($line -replace ".*SHA1:\s*", "") -ForegroundColor White
            } elseif ($line -match "SHA256") {
                Write-Host "SHA256: " -NoNewline -ForegroundColor Green
                Write-Host ($line -replace ".*SHA256:\s*", "") -ForegroundColor White
            }
        }
    } catch {
        Write-Host "Erro ao executar keytool: $_" -ForegroundColor Red
    }

    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
} else {
    Write-Host "ERRO: Debug keystore não encontrado em $debugKeystore" -ForegroundColor Red
    Write-Host ""
    Write-Host "CRIANDO DEBUG KEYSTORE..." -ForegroundColor Yellow
    Write-Host "----------------------------------------" -ForegroundColor Yellow

    try {
        & $keytoolPath -genkey -v -keystore $debugKeystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
        Write-Host ""
        Write-Host "Debug keystore criado! Execute o script novamente para obter os SHA fingerprints." -ForegroundColor Green
        Write-Host ""
    } catch {
        Write-Host "Erro ao criar debug keystore: $_" -ForegroundColor Red
    }
    exit 0
}

Write-Host ""
Write-Host "[3/4] Usando Gradle (Alternativo):" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow
Write-Host "Você também pode usar o comando Gradle:" -ForegroundColor White
Write-Host ".\gradlew signingReport" -ForegroundColor Cyan
Write-Host ""

Write-Host ""
Write-Host "[4/4] Instruções para adicionar no Firebase Console:" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow
Write-Host "1. Acesse: https://console.firebase.google.com" -ForegroundColor White
Write-Host "2. Selecione o projeto: unaspmarketplace" -ForegroundColor White
Write-Host "3. Vá em: Configurações do Projeto (ícone de engrenagem)" -ForegroundColor White
Write-Host "4. Aba: Geral" -ForegroundColor White
Write-Host "5. Role até 'Seus apps'" -ForegroundColor White
Write-Host "6. Clique no app Android: com.unasp.unaspmarketplace" -ForegroundColor White
Write-Host "7. Clique em 'Adicionar impressão digital'" -ForegroundColor White
Write-Host "8. Cole o SHA-1 (copie da linha acima que começa com SHA1:)" -ForegroundColor White
Write-Host "9. Clique em 'Salvar'" -ForegroundColor White
Write-Host "10. Repita para SHA-256" -ForegroundColor White
Write-Host ""

Write-Host ""
Write-Host "Informações do Projeto:" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow
Write-Host "Package Name: com.unasp.unaspmarketplace" -ForegroundColor White
Write-Host "Project ID: unaspmarketplace" -ForegroundColor White
Write-Host ""

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  PRÓXIMOS PASSOS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Copie os SHA-1 e SHA-256 acima" -ForegroundColor White
Write-Host "2. Adicione no Firebase Console (instruções acima)" -ForegroundColor White
Write-Host "3. Baixe o novo google-services.json" -ForegroundColor White
Write-Host "4. Substitua o arquivo em: app\google-services.json" -ForegroundColor White
Write-Host "5. Rebuild o projeto: .\gradlew clean build" -ForegroundColor White
Write-Host "6. Reinstale o app no dispositivo" -ForegroundColor White
Write-Host ""

Read-Host "Pressione Enter para continuar..."
