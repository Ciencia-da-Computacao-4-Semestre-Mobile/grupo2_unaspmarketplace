# 🎨 Usando Logo do UNASP Marketplace como Ícone do App

## ✅ Configuração Atualizada

### 📱 O que foi configurado:

1. **Foreground atualizado**
   - Agora usa: `@drawable/logounaspmarketplace`
   - Arquivo: `ic_app_logo_foreground.xml`
   - O logo PNG existente é usado como ícone principal

2. **Background personalizado**
   - Cores oficiais do UNASP Marketplace:
     - Azul principal: `#0073e6`
     - Azul secundário: `#005bb5` (overlay)
   - Arquivo: `ic_app_logo_background.xml`

3. **Adaptive Icons configurados**
   - `ic_launcher.xml` → usa o logo + background azul
   - `ic_launcher_round.xml` → versão redonda

## 🚀 Como Gerar Ícones em Todas as Resoluções (Recomendado)

Para ter o melhor resultado em TODOS os dispositivos Android, siga estes passos:

### Usando Android Studio (Image Asset Studio):

1. **Abra o Image Asset Studio:**
   ```
   Android Studio → Botão direito em 'res' → New → Image Asset
   ```

2. **Configure o Asset Type:**
   - Selecione: **Launcher Icons (Adaptive and Legacy)**

3. **Configure o Foreground Layer:**
   - Source Asset Type: **Image**
   - Path: Clique em 📁 e selecione:
     ```
     I:\Kotlin\grupo2_unaspmarketplace\app\src\main\res\drawable\logounaspmarketplace.png
     ```
   - Resize: Ajuste para **80-90%** (para dar espaço nas bordas)
   - Trim: **Yes** (remover espaços em branco)

4. **Configure o Background Layer:**
   - Source Asset Type: **Color**
   - Color: `#0073e6` (azul do UNASP)

5. **Options:**
   - Name: `ic_launcher` (manter)
   - ✅ Generate Legacy Icon: **Yes**
   - ✅ Generate Round Icon: **Yes**

6. **Clique em "Next" → "Finish"**

### Resultado Esperado:

O Android Studio irá gerar automaticamente:

```
mipmap-mdpi/
  ├── ic_launcher.webp (48x48dp)
  └── ic_launcher_round.webp

mipmap-hdpi/
  ├── ic_launcher.webp (72x72dp)
  └── ic_launcher_round.webp

mipmap-xhdpi/
  ├── ic_launcher.webp (96x96dp)
  └── ic_launcher_round.webp

mipmap-xxhdpi/
  ├── ic_launcher.webp (144x144dp)
  └── ic_launcher_round.webp

mipmap-xxxhdpi/
  ├── ic_launcher.webp (192x192dp)
  └── ic_launcher_round.webp

mipmap-anydpi-v26/
  ├── ic_launcher.xml (adaptive icon)
  └── ic_launcher_round.xml (adaptive icon round)
```

## 📋 Configuração Atual (Já Feita)

### ✅ Adaptive Icons (Android 8.0+)
- **Foreground:** Logo UNASP Marketplace
- **Background:** Azul #0073e6 com gradiente
- **Arquivos atualizados:**
  - `ic_launcher.xml`
  - `ic_launcher_round.xml`
  - `ic_app_logo_foreground.xml`
  - `ic_app_logo_background.xml`

### ⚠️ Legacy Icons (Android 7.1 e anteriores)
- Ainda usam os arquivos `.webp` padrão do Android
- **Recomendação:** Gerar novos usando Image Asset Studio (passos acima)

## 🎨 Cores do Ícone

| Elemento | Cor | Código |
|----------|-----|--------|
| Background principal | Azul UNASP | `#0073e6` |
| Background overlay | Azul escuro | `#005bb5` |
| Foreground | Logo PNG | `logounaspmarketplace.png` |

## ✅ Checklist de Qualidade

- [x] Adaptive icon configurado (Android 8.0+)
- [x] Logo UNASP sendo usado como foreground
- [x] Background com cores oficiais do app
- [x] Ícone redondo configurado
- [ ] **Recomendado:** Gerar versões PNG para Android 7.1- usando Image Asset Studio

## 📱 Preview

### Android 8.0+ (Adaptive)
```
┌─────────────────┐
│                 │
│   [Background]  │  ← Azul #0073e6
│   com gradiente │
│                 │
│  [Logo UNASP]   │  ← Centralizado
│                 │
└─────────────────┘
```

### Diferentes Formatos (Launcher)
- 🔵 Círculo (Google Pixel)
- ⬜ Quadrado (Samsung)
- 🔶 Squircle (OnePlus)
- 💧 Teardrop (Outros)

## 🔧 Troubleshooting

### O logo aparece muito pequeno?
→ Ajuste o `android:width` e `android:height` em `ic_app_logo_foreground.xml`

### O logo não está centralizado?
→ Use `android:gravity="center"` (já configurado)

### Quero usar só a cor de fundo sem gradiente?
→ Edite `ic_app_logo_background.xml` e remova o segundo `<path>` (overlay)

### O ícone não atualiza no dispositivo?
→ Desinstale completamente o app e reinstale

## 🎯 Próximo Passo Recomendado

**Execute o Image Asset Studio** seguindo os passos acima para gerar versões otimizadas em todas as resoluções. Isso garantirá que o ícone fique perfeito em todos os dispositivos Android!

---

**Status:** ✅ Configuração básica completa usando o logo UNASP!
**Recomendação:** Gerar assets completos via Image Asset Studio para melhor qualidade.

