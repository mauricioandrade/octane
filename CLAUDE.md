# Octane — Instruções para Claude Code

## Commits

- **Nunca** adicionar `Co-Authored-By:` nos commits deste projeto
- Seguir a convenção definida em `docs/commit-conventions.md`
- Commits semânticos: `type(scope): descrição`

## Injeção de Dependência (Backend)

- Sempre via construtor explícito, campos `private final`
- Nunca `@Autowired` em campos

## Stack

- Backend: Java 21, Spring Boot 4.0.6, Maven Wrapper (`./mvnw`)
- Frontend: Vite 6, React 19, TypeScript 5, TanStack Query 5
- Banco: PostgreSQL 16 via Docker Compose (`make dev-db`)
