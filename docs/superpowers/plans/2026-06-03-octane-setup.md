# Octane Setup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Criar o setup inicial completo do monorepo Octane — backend Spring Boot 4.0.6, frontend Vite/React/TS e infraestrutura local com Docker + Makefile.

**Architecture:** Monorepo com dois projetos independentes (`backend/` e `frontend/`) coordenados por um `Makefile` na raiz. O backend segue Clean Architecture com pacotes por domínio; o frontend é um SPA React com TanStack Query pronto para crescer. O banco de dados PostgreSQL sobe via Docker Compose.

**Tech Stack:** Java 21, Spring Boot 4.0.6, PostgreSQL 16, Flyway, Maven Wrapper, Vite 6, React 19, TypeScript 5, TanStack Query 5

---

## Mapa de Arquivos

| Arquivo | Responsabilidade |
|---------|-----------------|
| `docs/commit-conventions.md` | Padrão de commits semânticos do projeto |
| `.gitignore` | Ignorar target/, node_modules/, dist/, .env |
| `docker-compose.yml` | Serviço PostgreSQL para desenvolvimento local |
| `Makefile` | Atalhos dev-db, backend, frontend, build |
| `README.md` | Descrição e instruções de setup local |
| `backend/pom.xml` | Dependências e build do backend |
| `backend/.mvn/wrapper/maven-wrapper.properties` | Versão do Maven fixada |
| `backend/mvnw` | Script Unix do Maven Wrapper |
| `backend/src/main/java/com/octane/OctaneApplication.java` | Entry point Spring Boot |
| `backend/src/main/resources/application.yml` | Config com perfil `local` e variáveis de ambiente |
| `backend/src/main/java/com/octane/shared/config/HealthHandler.java` | GET /health → 200 OK |
| `backend/src/test/java/com/octane/shared/config/HealthHandlerTest.java` | Teste do endpoint /health |
| `frontend/package.json` | Dependências do frontend |
| `frontend/vite.config.ts` | Config Vite |
| `frontend/tsconfig.json` + `tsconfig.node.json` | Config TypeScript |
| `frontend/index.html` | Entry point HTML |
| `frontend/src/main.tsx` | Monta a app no DOM |
| `frontend/src/QueryProvider.tsx` | QueryClientProvider do TanStack Query |
| `frontend/src/App.tsx` | Placeholder com texto "Octane" |

---

## Task 1: Convenção de commits + remote origin

**Files:**
- Create: `docs/commit-conventions.md`

- [ ] **Step 1: Criar o documento de convenção de commits**

```markdown
# Convenção de Commits — Octane

Seguimos o padrão [Conventional Commits](https://www.conventionalcommits.org/).

## Formato

```
<type>(<scope>): <descrição>
```

- **type**: categoria da mudança (obrigatório)
- **scope**: contexto afetado (opcional)
- **descrição**: frase curta no imperativo, sem ponto final, máx. 72 chars

## Types

| Type | Quando usar |
|------|-------------|
| `feat` | Nova funcionalidade para o usuário |
| `fix` | Correção de bug |
| `docs` | Documentação apenas |
| `style` | Formatação, espaçamento — sem mudança de lógica |
| `refactor` | Refatoração sem novo comportamento nem bug fix |
| `test` | Adição ou correção de testes |
| `chore` | Tarefas de manutenção (deps, config, scripts) |
| `build` | Mudanças no sistema de build ou ferramentas externas |
| `ci` | Configuração de CI/CD |

## Scopes

| Scope | Contexto |
|-------|----------|
| `backend` | Código do servidor Spring Boot |
| `frontend` | Código do cliente React |
| `infra` | Docker, Makefile, scripts |
| `docs` | Documentação |

## Exemplos

```bash
feat(backend): add create station use case
fix(frontend): correct query invalidation on station update
docs: add commit conventions guide
build(backend): add maven wrapper
chore(infra): add docker-compose for local postgres
test(backend): add health check endpoint test
```

## Regras

- Use o imperativo: "add" não "added", "fix" não "fixed"
- Primeira letra minúscula na descrição
- Sem ponto final
- Commits pequenos e focados — um assunto por commit
- Breaking changes: adicione `!` após o type/scope, ex: `feat(backend)!: change station API response format`
```

Salvar em `docs/commit-conventions.md`.

- [ ] **Step 2: Adicionar remote origin**

```bash
cd /home/mauricio/projetos/octane
git remote add origin git@github.com:mauricioandrade/octane.git
git remote -v
```

Esperado:
```
origin  git@github.com:mauricioandrade/octane.git (fetch)
origin  git@github.com:mauricioandrade/octane.git (push)
```

- [ ] **Step 3: Commit**

```bash
git add docs/commit-conventions.md
git commit -m "docs: add commit conventions guide"
```

---

## Task 2: .gitignore, docker-compose, Makefile e README

**Files:**
- Create: `.gitignore`
- Create: `docker-compose.yml`
- Create: `Makefile`
- Create: `README.md`

- [ ] **Step 1: Criar .gitignore na raiz**

```gitignore
# Backend
backend/target/
backend/.mvn/wrapper/maven-wrapper.jar
*.class

# Frontend
frontend/node_modules/
frontend/dist/
frontend/.vite/

# Environment
.env
.env.local

# IDE
.idea/
.vscode/
*.iml

# OS
.DS_Store
Thumbs.db
```

- [ ] **Step 2: Criar docker-compose.yml**

```yaml
services:
  postgres:
    image: postgres:16
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: octane_db
      POSTGRES_USER: octane
      POSTGRES_PASSWORD: octane
```

- [ ] **Step 3: Criar Makefile**

```makefile
.PHONY: dev-db backend frontend build

dev-db:
	docker compose up -d postgres

backend:
	cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local

frontend:
	cd frontend && npm run dev

build:
	cd backend && ./mvnw package && cd ../frontend && npm run build
```

> Atenção: cada linha de receita no Makefile **deve** usar TAB, não espaços.

- [ ] **Step 4: Criar README.md**

```markdown
# Octane

Sistema de gestão para postos de combustível.

## Pré-requisitos

- Java 21+
- Node 20+
- Docker

## Setup local

1. Suba o banco de dados:
   ```bash
   make dev-db
   ```

2. Suba o backend:
   ```bash
   make backend
   ```

3. Suba o frontend:
   ```bash
   make frontend
   ```

## Endpoints

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | /health | Health check |

## Convenção de commits

Ver [docs/commit-conventions.md](docs/commit-conventions.md).
```

- [ ] **Step 5: Commit**

```bash
git add .gitignore docker-compose.yml Makefile README.md
git commit -m "chore(infra): add gitignore, docker-compose, Makefile and README"
```

---

## Task 3: Backend — pom.xml e Maven Wrapper

**Files:**
- Create: `backend/pom.xml`
- Generate: `backend/mvnw`, `backend/.mvn/wrapper/maven-wrapper.properties`

- [ ] **Step 1: Criar estrutura de diretórios do backend**

```bash
mkdir -p backend/src/main/java/com/octane/station/domain
mkdir -p backend/src/main/java/com/octane/station/usecase
mkdir -p backend/src/main/java/com/octane/station/repository
mkdir -p backend/src/main/java/com/octane/station/handler
mkdir -p backend/src/main/java/com/octane/shared/exception
mkdir -p backend/src/main/java/com/octane/shared/config
mkdir -p backend/src/main/resources/db/migration
mkdir -p backend/src/test/java/com/octane/shared/config
```

- [ ] **Step 2: Criar backend/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.6</version>
        <relativePath/>
    </parent>

    <groupId>com.octane</groupId>
    <artifactId>octane-backend</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>octane-backend</name>
    <description>Octane — Sistema de gestão para postos de combustível</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: Gerar Maven Wrapper**

```bash
cd backend && mvn -N wrapper:wrapper
```

Esperado: geração dos arquivos `mvnw`, `mvnw.cmd` e `.mvn/wrapper/maven-wrapper.properties`.

- [ ] **Step 4: Tornar mvnw executável**

```bash
chmod +x backend/mvnw
```

- [ ] **Step 5: Commit**

```bash
git add backend/pom.xml backend/mvnw backend/mvnw.cmd backend/.mvn
git commit -m "build(backend): add pom.xml and maven wrapper"
```

---

## Task 4: Backend — Application entry point e configuração

**Files:**
- Create: `backend/src/main/java/com/octane/OctaneApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `.gitkeep` nos pacotes vazios

- [ ] **Step 1: Criar OctaneApplication.java**

```java
package com.octane;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OctaneApplication {

    public static void main(String[] args) {
        SpringApplication.run(OctaneApplication.class, args);
    }
}
```

- [ ] **Step 2: Criar application.yml**

```yaml
spring:
  application:
    name: octane

---
spring:
  config:
    activate:
      on-profile: local
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/octane_db}
    username: ${DB_USER:octane}
    password: ${DB_PASS:octane}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
    locations: classpath:db/migration
```

- [ ] **Step 3: Adicionar .gitkeep nos pacotes e migration vazios**

```bash
touch backend/src/main/java/com/octane/station/domain/.gitkeep
touch backend/src/main/java/com/octane/station/usecase/.gitkeep
touch backend/src/main/java/com/octane/station/repository/.gitkeep
touch backend/src/main/java/com/octane/station/handler/.gitkeep
touch backend/src/main/java/com/octane/shared/exception/.gitkeep
touch backend/src/main/resources/db/migration/.gitkeep
```

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/octane/OctaneApplication.java \
        backend/src/main/resources/application.yml \
        backend/src/main/java/com/octane/station \
        backend/src/main/java/com/octane/shared/exception \
        backend/src/main/resources/db/migration
git commit -m "chore(backend): add application entry point, config and package structure"
```

---

## Task 5: Backend — Health Check (TDD)

**Files:**
- Create: `backend/src/test/java/com/octane/shared/config/HealthHandlerTest.java`
- Create: `backend/src/main/java/com/octane/shared/config/HealthHandler.java`

- [ ] **Step 1: Escrever o teste antes da implementação**

```java
package com.octane.shared.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthHandler.class)
class HealthHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void health_returns200WithOkBody() throws Exception {
        mockMvc.perform(get("/health"))
               .andExpect(status().isOk())
               .andExpect(content().string("ok"));
    }
}
```

- [ ] **Step 2: Rodar o teste e confirmar que falha**

```bash
cd backend && ./mvnw test -Dtest=HealthHandlerTest
```

Esperado: FAIL com `ClassNotFoundException: com.octane.shared.config.HealthHandler` ou erro de compilação.

- [ ] **Step 3: Implementar HealthHandler.java**

```java
package com.octane.shared.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthHandler {

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }
}
```

- [ ] **Step 4: Rodar o teste e confirmar que passa**

```bash
cd backend && ./mvnw test -Dtest=HealthHandlerTest
```

Esperado:
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

- [ ] **Step 5: Commit**

```bash
git add backend/src/test/java/com/octane/shared/config/HealthHandlerTest.java \
        backend/src/main/java/com/octane/shared/config/HealthHandler.java
git commit -m "feat(backend): add health check endpoint"
```

---

## Task 6: Frontend — Scaffold completo

**Files:**
- Create: `frontend/.gitignore`
- Create: `frontend/package.json`
- Create: `frontend/vite.config.ts`
- Create: `frontend/tsconfig.json`
- Create: `frontend/tsconfig.node.json`
- Create: `frontend/index.html`
- Create: `frontend/src/main.tsx`
- Create: `frontend/src/QueryProvider.tsx`
- Create: `frontend/src/App.tsx`

- [ ] **Step 1: Criar frontend/.gitignore**

```gitignore
node_modules/
dist/
.vite/
*.local
```

- [ ] **Step 2: Criar frontend/package.json**

```json
{
  "name": "octane-frontend",
  "private": true,
  "version": "0.0.1",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "@tanstack/react-query": "^5.0.0",
    "react": "^19.0.0",
    "react-dom": "^19.0.0"
  },
  "devDependencies": {
    "@types/react": "^19.0.0",
    "@types/react-dom": "^19.0.0",
    "@vitejs/plugin-react": "^4.0.0",
    "typescript": "^5.0.0",
    "vite": "^6.0.0"
  }
}
```

- [ ] **Step 3: Criar frontend/vite.config.ts**

```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
})
```

- [ ] **Step 4: Criar frontend/tsconfig.json**

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

- [ ] **Step 5: Criar frontend/tsconfig.node.json**

```json
{
  "compilerOptions": {
    "composite": true,
    "skipLibCheck": true,
    "module": "ESNext",
    "moduleResolution": "bundler",
    "allowSyntheticDefaultImports": true
  },
  "include": ["vite.config.ts"]
}
```

- [ ] **Step 6: Criar frontend/index.html**

```html
<!doctype html>
<html lang="pt-BR">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Octane</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>
```

- [ ] **Step 7: Criar frontend/src/QueryProvider.tsx**

```tsx
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ReactNode } from 'react'

const queryClient = new QueryClient()

interface QueryProviderProps {
  children: ReactNode
}

export function QueryProvider({ children }: QueryProviderProps) {
  return (
    <QueryClientProvider client={queryClient}>
      {children}
    </QueryClientProvider>
  )
}
```

- [ ] **Step 8: Criar frontend/src/App.tsx**

```tsx
export function App() {
  return <h1>Octane</h1>
}
```

- [ ] **Step 9: Criar frontend/src/main.tsx**

```tsx
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryProvider } from './QueryProvider'
import { App } from './App'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryProvider>
      <App />
    </QueryProvider>
  </StrictMode>
)
```

- [ ] **Step 10: Instalar dependências**

```bash
cd frontend && npm install
```

Esperado: criação de `node_modules/` e `package-lock.json` sem erros.

- [ ] **Step 11: Commit**

```bash
git add frontend/
git commit -m "build(frontend): scaffold Vite + React + TypeScript + TanStack Query"
```

---

## Task 7: Push inicial para o remote

- [ ] **Step 1: Verificar remote**

```bash
git remote -v
```

Esperado:
```
origin  git@github.com:mauricioandrade/octane.git (fetch)
origin  git@github.com:mauricioandrade/octane.git (push)
```

- [ ] **Step 2: Renomear branch para main e fazer push**

```bash
git branch -m master main
git push -u origin main
```

Esperado: push bem-sucedido com todos os commits.
