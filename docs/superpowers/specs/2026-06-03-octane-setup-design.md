# Octane — Setup Inicial do Monorepo

**Data:** 2026-06-03
**Status:** Aprovado

---

## Visão Geral

Setup inicial do monorepo **Octane**, sistema de gestão para postos de combustível. Nenhuma entidade de negócio ainda — apenas estrutura, configuração de banco e health check.

---

## Estrutura do Monorepo

```
octane/
├── backend/
│   ├── .mvn/wrapper/
│   ├── mvnw / mvnw.cmd
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/octane/
│       │   ├── OctaneApplication.java
│       │   ├── station/
│       │   │   ├── domain/
│       │   │   ├── usecase/
│       │   │   ├── repository/
│       │   │   └── handler/
│       │   └── shared/
│       │       ├── exception/
│       │       └── config/
│       │           └── HealthHandler.java
│       └── main/resources/
│           ├── application.yml
│           └── db/migration/
├── frontend/
│   ├── index.html
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   └── src/
│       ├── main.tsx
│       ├── App.tsx
│       └── QueryProvider.tsx
├── docker-compose.yml
├── Makefile
└── README.md
```

---

## Backend

- **Runtime:** Java 21, Spring Boot 4.0.6, packaging `jar`
- **Dependências:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `postgresql`, `flyway-core`
- **Maven Wrapper** incluso (`mvnw`) — não depende de Maven instalado globalmente
- **Perfil:** `local`, variáveis de ambiente com defaults para docker-compose

### application.yml

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/octane_db}
    username: ${DB_USER:octane}
    password: ${DB_PASS:octane}
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
```

### Health Check

`GET /health` → `200 OK`, body `"ok"`. Implementado em `shared/config/HealthHandler.java`.

### Convenção de DI

- Nunca `@Autowired` em campos
- Sempre injeção via construtor explícito
- Campos `private final`

```java
@Service
public class ExampleService {

    private final SomeDependency dep;

    public ExampleService(SomeDependency dep) {
        this.dep = dep;
    }
}
```

---

## Frontend

- **Stack:** Vite + React + TypeScript
- **Dependências extras:** `@tanstack/react-query`
- `App.tsx` — placeholder com `<h1>Octane</h1>`
- `QueryProvider.tsx` — envolve a app com `QueryClientProvider`
- `main.tsx` — monta `<QueryProvider><App /></QueryProvider>`

---

## Docker Compose

Apenas PostgreSQL:

| Parâmetro | Valor |
|-----------|-------|
| Imagem | `postgres:16` |
| Porta | `5432` |
| Database | `octane_db` |
| User | `octane` |
| Password | `octane` |

---

## Makefile

| Target | Comando |
|--------|---------|
| `dev-db` | `docker compose up -d postgres` |
| `backend` | `cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local` |
| `frontend` | `cd frontend && npm run dev` |
| `build` | `cd backend && ./mvnw package && cd ../frontend && npm run build` |

---

## README

- Nome: **Octane**
- Descrição: Sistema de gestão para postos de combustível
- Pré-requisitos: Java 21, Node 24+, Docker
- Setup: `make dev-db` → `make backend` → `make frontend`
