# Convenção de Commits — Octane

Seguimos o padrão [Conventional Commits](https://www.conventionalcommits.org/).

## Formato

```
<type>(<scope>): <descrição>
```

- **type**: categoria da mudança (obrigatório)
- **scope**: contexto afetado (opcional)
- **descrição**: frase curta no imperativo, sem ponto final — 72 chars total na subject line

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
| `revert` | Reversão de um commit anterior |

## Scopes

| Scope | Contexto |
|-------|----------|
| `backend` | Código do servidor Spring Boot |
| `frontend` | Código do cliente React |
| `infra` | Docker, Makefile, scripts |

> Use `docs:` sem scope para mudanças de documentação em geral. Reserve `docs(docs):` para quando existir uma pasta `docs/` com sub-seções próprias que precisem de escopo.

Quando uma mudança atravessa múltiplos scopes, omita o scope: `refactor: extract shared validation logic`.

## Exemplos

```text
feat(backend): add create station use case
fix(frontend): correct query invalidation on station update
docs: add commit conventions guide
build(backend): add maven wrapper
chore(infra): add docker-compose for local postgres
test(backend): add health check endpoint test
```

## Body e Footer

O body e o footer são separados da subject line por uma linha em branco:

```text
feat(backend): add rate limiting to station API

Implementa token bucket por IP com limite de 100 req/min.
Configura via application.yml.

BREAKING CHANGE: o header X-RateLimit-Limit agora é obrigatório nas respostas.
```

- **Body**: contexto e motivação da mudança (opcional)
- **Footer**: tokens como `BREAKING CHANGE:`, `Closes #123`, `Co-authored-by:` (opcional)

## Regras

- Use o imperativo: "add" não "added", "fix" não "fixed"
- Primeira letra minúscula na descrição
- Sem ponto final
- Commits pequenos e focados — um assunto por commit
- Breaking changes: adicione `!` após o type/scope, ex: `feat(backend)!: change station API response format`
- Mudanças que afetam múltiplos scopes: omita o scope e descreva o contexto no body
