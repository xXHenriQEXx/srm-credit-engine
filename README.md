# SRM Credit Engine

Plataforma de cessão de crédito multimoedas (FIDC) — teste técnico para vaga de Engenheiro de Software Pleno na SRM Asset.

## Stack

| Camada     | Tecnologia                                      |
|------------|--------------------------------------------------|
| Backend    | Java 21 + Spring Boot 3.3 (Web, Data JPA, Validation) |
| Banco      | PostgreSQL 16 + Flyway (migrations versionadas)  |
| Frontend   | Angular 18 (standalone components, Reactive Forms) |
| Docs API   | OpenAPI/Swagger (springdoc-openapi)              |
| Orquestração | Docker + Docker Compose                        |

## Como rodar

Pré-requisito: Docker e Docker Compose instalados.

```bash
docker compose up --build
```

- Backend: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Frontend: http://localhost:4200
- Postgres: localhost:5432 (`srm_user` / `srm_pass` / db `srm_credit_engine`)

O schema do banco é criado automaticamente pelo Flyway na primeira subida (`V1__init_schema.sql`), incluindo o cadastro inicial de BRL/USD e uma taxa de câmbio seed.

### Rodando localmente sem Docker

```bash
# Backend (requer Postgres rodando localmente, ver application.yml)
cd backend
mvn spring-boot:run

# Frontend
cd frontend
npm install
npm start   # http://localhost:4200, aponta para localhost:8080 (ver src/environments)
```

### Testes

```bash
cd backend
mvn test
```

Cobertura: estratégias de risco (Strategy Pattern) e motor de precificação (`PricingService`), incluindo casos de conversão cross-currency e rejeição de vencimento no passado.

## Arquitetura

```
Frontend (Angular)  →  REST API (Spring)  →  Service (regras de negócio)  →  Repository (JPA / JDBC)  →  PostgreSQL
```

- **Camada de Apresentação** (`controller`): validação de forma (Bean Validation), serialização, códigos HTTP.
- **Camada de Negócio** (`service`): regras de precificação, orquestração da persistência transacional, conversão cambial.
- **Camada de Persistência** (`repository`, `entity`): acesso a dados via Spring Data JPA.
- **Exceção à regra**: o relatório de **Extrato de Liquidação** (`SettlementExtractController` → `SettlementExtractRepository`) fala direto com SQL nativo via `JdbcTemplate`, pulando a camada de negócio — conforme permitido no escopo do teste, já que não há regra de negócio a aplicar em uma consulta de leitura, apenas filtragem/paginação, e o SQL nativo dá controle fino sobre os índices usados em consultas analíticas de grande volume.

### Motor de Precificação (Strategy Pattern)

```
PricingService  →  RiskSpreadStrategyFactory  →  RiskSpreadStrategy (interface)
                                                     ├── DuplicataMercantilStrategy (spread 1.5% a.m.)
                                                     └── ChequePreDatadoStrategy   (spread 2.5% a.m.)
```

O `PricingService` não conhece as regras de risco de cada tipo de recebível — ele apenas pede ao `RiskSpreadStrategyFactory` a estratégia correspondente ao `ReceivableType` recebido e aplica a fórmula genérica:

```
Valor Presente = Valor Face / (1 + Taxa Base + Spread) ^ Prazo(meses)
```

Adicionar um novo tipo de recebível = criar uma nova classe `@Component` implementando `RiskSpreadStrategy`. Nenhuma classe existente precisa ser alterada (Open/Closed Principle) — o Spring injeta automaticamente todas as estratégias disponíveis na factory.

Se `faceCurrency != settlementCurrency`, o valor presente é convertido usando a taxa de câmbio mais recente cadastrada (`CurrencyExchangeService`), lançando erro de negócio (HTTP 422) caso não exista taxa cadastrada para o par de moedas.

### Integridade transacional (ACID)

`TransactionService.createAndSettle` é anotado com `@Transactional`: a precificação, a resolução das moedas e o `INSERT` da transação acontecem atomicamente. Qualquer falha (moeda inexistente, erro de banco) faz rollback completo — nenhuma liquidação fica "pela metade". A entidade `Transaction` também usa **optimistic locking** (`@Version`), protegendo contra condições de corrida em atualizações concorrentes sobre o mesmo registro.

### Tratamento de exceções

Um `@RestControllerAdvice` (`GlobalExceptionHandler`) centraliza o tratamento: erros de validação viram HTTP 400 com a lista de campos inválidos, erros de negócio (moeda/taxa não encontrada, tipo de recebível não suportado) viram HTTP 404/422, conflitos de concorrência viram HTTP 409, e qualquer exceção não mapeada é convertida em HTTP 500 genérico — nunca vazamos stacktrace ou mensagens de driver JDBC para o cliente.

## Modelagem de Dados

Ver [`docs/ER-diagram.md`](docs/ER-diagram.md) (diagrama + decisões de modelagem) e [`docs/ddl.sql`](docs/ddl.sql) (script DDL completo, idêntico à migration Flyway `V1__init_schema.sql`).

## Critérios de Aceite (resumo)

- **Usabilidade**: simulação em tempo real no Painel do Operador (debounce de 400ms sobre o formulário, sem necessidade de clicar em "calcular"); mensagens de erro de negócio exibidas de forma legível ao operador.
- **Segurança**: validação de entrada em duas camadas (Bean Validation no DTO + regras de negócio no service, ex: data de vencimento no passado); nenhuma informação sensível de erro interno é exposta na API.
- **Desempenho**: relatório de extrato usa SQL nativo com paginação `LIMIT/OFFSET` no banco (nunca traz o dataset inteiro para a aplicação) e índices dedicados em `assignor_name`, `created_at` e `settlement_currency`.
- **Escalabilidade**: camadas desacopladas (controller/service/repository) e Strategy Pattern permitem evoluir regras de risco e tipos de recebível sem reescrever o motor de cálculo.

## Fluxo de Git adotado

Commits seguem **Conventional Commits** (`feat:`, `fix:`, `docs:`, `test:`, `chore:`). O desenvolvimento foi organizado em branches de feature (`feature/currency-engine`, `feature/pricing-strategy`, `feature/transaction-api`, `feature/operator-panel`, `feature/transaction-grid`), simulando Pull Requests mesmo em desenvolvimento solo, conforme pedido no teste para o nível Pleno.

## Uso de IA

Ver [`AI_USAGE.md`](AI_USAGE.md).
