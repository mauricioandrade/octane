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
