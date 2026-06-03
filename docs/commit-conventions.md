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
