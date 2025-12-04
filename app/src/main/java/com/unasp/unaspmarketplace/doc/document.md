Documentação Técnica Completa — UNASP Marketplace (versão detalhada)
1. Resumo do projeto

Nome: UNASP Marketplace
Tipo: Aplicativo móvel (Android, Kotlin) — marketplace/e-commerce focado no público universitário.
Objetivo: automatizar o atendimento/compras internas (produtos da faculdade / comunidade), fornecer cadastro de produtos/pedidos, controle de usuários, histórico, e preparação para publicação na Play Store. Fonte: briefing do projeto.

2. Escopo e Regras de Negócio (Business Rules)
   2.1 Usuários e papéis

Usuário (Cliente / Aluno): pode navegar, adicionar ao carrinho, realizar pedidos, ver histórico e cancelar (dentro de regras).

Administrador / Vendedor: gerencia produtos (CRUD), confirma/atualiza status de pedidos, visualiza relatórios.

Guest (visitante): pode navegar e ver produtos; cadastro/login necessário para comprar.

2.2 Regras de pedido / checkout

Carrinho persistente por usuário (local + sincronizado com backend se houver sessão).

Pedido só é criado após pagamento confirmado / seleção de método (pagamento offline = status PENDENTE até confirmação).

Cancelamento:

Cliente pode cancelar enquanto o pedido estiver em status PENDENTE.

Depois de EM_PREPARO apenas admin pode cancelar (com justificativa).

Estoque:

Ao criar pedido o estoque pode ser reservado (opcional) ou debitado imediatamente.

Não permitir compra se stock <= 0.

Frete / Retirada:

Opção de retirada (na faculdade) com taxa zero ou frete com valor calculado.

Histórico:

Todos os status e timestamps devem ser armazenados (created_at, updated_at, status_history).

2.3 Validações importantes

Email único por usuário.

CPF (se coletado) validado com regra local (se aplicável).

Preço não negativo, quantidade mínima 1.

Senhas com mínimo (ex: 8 chars) + recomendações de segurança.

3. Arquitetura proposta (alta-nível)
   3.1 Padrões e camadas

Arquitetura recomendada: MVVM (Model-View-ViewModel) + Clean Architecture (camadas: data, domain, presentation).

Coroutines + Flow para assincronismo.

Dependency Injection: Hilt (recomendado) ou Koin.

Persistência local: Room para cache/DB local (usuário, carrinho, preferências).

Network: Retrofit + OkHttp (interceptadores para logs + tratamento de erros).

Imagens: Coil/Glide.
Essas tecnologias foram sugeridas no briefing do projeto.

3.2 Módulos (sugestão)

app — módulo Android (UI + DI wiring).

data — implementações (API, Room, mappers).

domain — casos de uso (usecases / business logic).

shared — modelos/dtos comuns (opcional).

4. Endpoints / Rotas API (sugestão RESTful)

Observação: se o backend já existir, adapte nomes/URIs. Se não, estes endpoints servem como especificação para backend.

4.1 Autenticação

POST /api/auth/register

Body: { "name","email","password","phone" }

Retorno: { user, token }

POST /api/auth/login

Body: { "email","password" }

Retorno: { user, token }

POST /api/auth/refresh — troca de refresh token (se implementado).

POST /api/auth/logout — revogar token.

4.2 Usuários

GET /api/users/{id}

PUT /api/users/{id} — atualizar perfil.

GET /api/users/{id}/orders — histórico de pedidos.

4.3 Produtos

GET /api/products — listagem (query params: q, category, page, limit, sort).

GET /api/products/{id}

POST /api/products — (admin) criar.

PUT /api/products/{id} — (admin) atualizar.

DELETE /api/products/{id} — (admin) deletar.

4.4 Categorias

GET /api/categories

POST /api/categories (admin)

4.5 Carrinho & Checkout

GET /api/cart — obter carrinho do usuário.

POST /api/cart — adicionar item { productId, quantity }.

PUT /api/cart/{itemId} — atualizar quantidade.

DELETE /api/cart/{itemId} — remover item.

POST /api/orders — criar pedido (body contém pagamento, endereço, items).

GET /api/orders/{id}

PUT /api/orders/{id}/cancel — cancelar pedido (regras aplicam).

GET /api/orders — listar pedidos (para admin).

4.6 Pagamentos (integração)

POST /api/payments/create — criar pagamento (integra com gateway).

POST /api/payments/webhook — webhook do gateway para confirmar pagamento.

4.7 Notificações

POST /api/notifications/send — (admin) enviar notificação.

5. Modelos de Dados (Schema sugerido)
   5.1 Tabelas principais (exemplo SQL)
   users
   CREATE TABLE users (
   id SERIAL PRIMARY KEY,
   name VARCHAR(200) NOT NULL,
   email VARCHAR(200) UNIQUE NOT NULL,
   password_hash VARCHAR(255) NOT NULL,
   phone VARCHAR(20),
   role VARCHAR(20) DEFAULT 'CLIENT',
   created_at TIMESTAMP DEFAULT now(),
   updated_at TIMESTAMP
   );

products
CREATE TABLE products (
id SERIAL PRIMARY KEY,
name VARCHAR(255) NOT NULL,
description TEXT,
price NUMERIC(10,2) NOT NULL,
stock INT DEFAULT 0,
category_id INT,
image_url TEXT,
created_at TIMESTAMP DEFAULT now(),
updated_at TIMESTAMP
);

categories
CREATE TABLE categories (
id SERIAL PRIMARY KEY,
name VARCHAR(100) UNIQUE NOT NULL
);

orders
CREATE TABLE orders (
id SERIAL PRIMARY KEY,
user_id INT REFERENCES users(id),
status VARCHAR(50) DEFAULT 'PENDING',
total NUMERIC(10,2),
payment_method VARCHAR(50),
shipping_address JSONB,
created_at TIMESTAMP DEFAULT now(),
updated_at TIMESTAMP
);

order_items
CREATE TABLE order_items (
id SERIAL PRIMARY KEY,
order_id INT REFERENCES orders(id) ON DELETE CASCADE,
product_id INT,
quantity INT,
unit_price NUMERIC(10,2)
);

cart_items (se usar persistência server-side)
CREATE TABLE cart_items (
id SERIAL PRIMARY KEY,
user_id INT REFERENCES users(id),
product_id INT,
quantity INT DEFAULT 1,
added_at TIMESTAMP DEFAULT now()
);

6. Fluxos de usuário (sequências)
   6.1 Fluxo de Login / Registro

Usuário abre app → Splash → verifica token local.

Se token válido → vai para home; senão → tela de login/opção registrar.

Ao registrar → POST /api/auth/register → recebe token → salva em SharedPreferences/DataStore → navega ao onboarding/home.

6.2 Fluxo de navegação e compra (básico)

Home → listagem de produtos (GET /api/products).

Usuário seleciona produto → vê detalhes → clica “Adicionar ao carrinho”.

Carrinho → revisar itens → clicar “Checkout”.

Checkout → escolher endereço/pagamento → POST /api/orders.

Se pagamento externo → abrir webView/SDK → callback/webhook confirma → order.status = CONFIRMED.

Usuário vê histórico.

6.3 Fluxo de cancelamento

Usuário acessa pedido em status PENDING → clica “Cancelar”.

App chama PUT /api/orders/{id}/cancel.

Backend verifica regras → atualiza status e, se necessário, devolve estoque.

7. Estrutura de pastas (Android — exemplo)
   app/
   ├─ src/
   │  ├─ main/
   │  │  ├─ java/com/unasp/unaspmarketplace/
   │  │  │  ├─ data/           # Repos, network, dto, mappers
   │  │  │  ├─ domain/         # models, usecases
   │  │  │  ├─ di/             # Hilt/Koin modules
   │  │  │  ├─ ui/
   │  │  │  │  ├─ features/    # screens por feature (product, cart, auth, order)
   │  │  │  ├─ utils/
   │  │  ├─ res/
   │  │  ├─ AndroidManifest.xml
   ├─ build.gradle(.kts)

8. Telas e Navegação (UI)
   Telas principais

Splash / Onboarding (opcional)

Login / Registro

Home (promoções, categorias)

Listagem de Produtos (filtros)

Detalhe do Produto

Carrinho

Checkout (endereço, pagamento)

Perfil / Configurações

Histórico de Pedidos / Detalhe do Pedido

Tela Admin (se previsto) — gerenciar produtos / pedidos

Navegação

Recomendo usar Jetpack Navigation (NavHostFragment) ou Navigation Compose (se usar Compose).

Deep links para abrir produto/pedido específico: unasp://product/{id}.

9. Padrões de implementação e boas práticas
   9.1 UI & State

ViewModels expõem StateFlow / LiveData (preferir StateFlow).

UI reativa a estados: Loading, Success(data), Error(message).

9.2 Error Handling

Centralizar tratamento de erros de rede em interceptors e wrappers (Result / Resource classes).

9.3 Segurança

Tokens JWT armazenados em EncryptedSharedPreferences ou DataStore com criptografia.

Comunicação via HTTPS.

Evitar logs de dados sensíveis.

Validação server-side de tudo (preço total não confiável vindo do cliente).

9.4 Testes

Unit tests: Use JUnit5 + Mockk/Mockito para ViewModel e UseCases.

Instrumentation: Espresso para flows críticos (login, checkout).

Testes de integração: testar endpoints mockados com MockWebServer (Retrofit).

10. Build / CI / Release
    10.1 Build

Gradle Kotlin DSL (ex.: build.gradle.kts), configure minSdk, targetSdk, compileSdk.

Use buildTypes (debug/release) e productFlavors se necessário.

10.2 Assinatura & Release (Google Play)

Gerar AAB assinado (recomendado).

Configurar keystore seguro (não commitar no repo).

Preparar play store listing: ícones, screenshots, descrição e política de privacidade.

10.3 CI/CD (sugestão)

GitHub Actions pipeline:

lint, assembleDebug, unitTest

assembleRelease com secrets para keystore (apenas no branch main ou tags)

(Opcional) fastlane para upload automático ao Play Console internal track.

11. Observabilidade & Logs

Integrar Crashlytics (Firebase) para crashes.

Usar Sentry / Bugsnag se preferir.

Monitoramento de eventos (mixpanel, firebase analytics) — importante para métricas de uso.

12. Documentação de API / Postman

Fornecer uma collection Postman com todas as rotas (auth, products, cart, orders).

Exemplos de payloads e respostas com códigos HTTP (200, 201, 400, 401, 403, 404, 500).

13. Checklist pré-entrega (para a disciplina / banca)

Código compilando em main.

APK/AAB assinado gerado.

README com instruções de build.

Documentação técnica (este documento + diagramas).

Testes unitários/instrumentação rodando.

Política de privacidade (se coleta dados).

Banner e material visual prontos (você enviou um briefing para isso).

14. Exemplos concretos (payloads e sequence)
    14.1 Exemplo — criar pedido (POST /api/orders)

Request:

{
"userId": 123,
"items": [
{ "productId": 11, "quantity": 2 },
{ "productId": 15, "quantity": 1 }
],
"paymentMethod": "CREDIT_CARD",
"shippingAddress": {
"street":"Rua X","city":"Cidade","zip":"00000-000"
},
"total": 75.50
}


Response 201:

{
"orderId": 987,
"status": "PENDING",
"createdAt": "2025-12-02T12:34:56Z"
}

14.2 Sequence diagram (texto)

App → Backend POST /api/orders

Backend valida estoque e calcula total

Backend cria pedido PENDING e responde

App → iniciar pagamento (SDK / redirect)

Gateway → webhook → Backend confirma pagamento

Backend atualiza pedido CONFIRMED → notifica cliente (push/email)

15. Documentos e artefatos que posso gerar para você (pronto para colar no repo)

Posso gerar agora, neste chat, os seguintes arquivos (em Markdown/MD) completos e prontos para commitar:

README.md com instruções de setup, build e run (Android Studio).

ARCHITECTURE.md detalhando camadas, pacotes e diagramas UML/textuais.

API_DOCUMENTATION.md com todas as rotas, exemplos de request/response, códigos de erro.

DB_SCHEMA.md com scripts SQL sugeridos.

DEV_GUIDE.md com boas práticas, padrões usados, convenções de commits.

PLAYSTORE_CHECKLIST.md com passos para assinar e publicar.

Diga quais desses você quer que eu gere agora — eu produzo o conteúdo pronto para você colar. (Se preferir, posso já incluir exemplos adaptados ao código real caso você me envie o README do repo ou me permita reler o ZIP).

16. O que eu usei e o que não pude obter automaticamente

Usei o briefing do projeto e o documento de banner que você subiu para referenciar requisitos, etapas e recomendações.

Não consegui extrair/ler automaticamente todo o código dentro do .zip que você também enviou nesta sessão (por isso não incluí nomes de classes, rotas hard-coded, ou detalhes exatos de implementação de código). Se quiser, eu posso gerar arquivos prontos (README, API docs etc.) e depois você adapte os nomes conforme o código, ou então me enviar o README/arquivos mais importantes para eu gerar uma versão 1:1 com o repo.

17. Próximos passos recomendados

Me diga se você quer que eu gere agora os arquivos Markdown listados (README, API_DOC, DB_SCHEMA, etc.).

Se quiser documentação alinhada 100% ao código, envie o README.md do repo ou permita que eu leia novamente o ZIP.

Se preferir, eu já gero os MDs com placeholders (ex.: BASE_URL, JWT_SECRET_NAME, KESTORE_PASS) — você só substitui valores sensíveis.

18. Modelo de README inicial (prévia)

Abaixo um resumo que eu posso transformar em README.md completo — quer que eu gere o arquivo agora (com conteúdo pronto para commit)?

(prévia curta)

# UNASP Marketplace

## Overview
Aplicativo Android em Kotlin - Marketplace para a comunidade UNASP.

## Tecnologias
- Kotlin, MVVM, Coroutines, Retrofit, Room, Hilt

## Como rodar (dev)
1. Clone repo
2. Abra no Android Studio
3. Configure variáveis (API_BASE_URL, KEYS)
4. Run -> app

## Build release
1. ./gradlew assembleRelease
2. Assinar AAB com keystore