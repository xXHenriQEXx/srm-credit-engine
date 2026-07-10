# Uso de IA neste projeto

> ⚠️ **Nota para quem for entregar este teste**: este arquivo descreve o processo real de construção do projeto com apoio de IA (Claude). Antes de submeter, releia cada seção do código gerado, garanta que você consegue explicar cada decisão em uma entrevista técnica, e ajuste este documento para refletir fielmente a sua experiência — inclusive removendo ou completando pontos que não se aplicaram ao seu fluxo de trabalho.

## Ferramenta utilizada

Claude (Anthropic), via chat, para geração assistida de código, arquitetura e documentação.

## Prompts estratégicos utilizados

- **Scaffolding inicial**: pedido de estrutura de projeto Spring Boot em camadas (controller/service/repository/entity) já separando a pasta de estratégias de risco (`strategy/`), para deixar o Strategy Pattern explícito na arquitetura desde o início, em vez de nascer como um `switch/case` a ser refatorado depois.
- **Motor de precificação**: prompt específico pedindo a fórmula `Valor Presente = Valor Face / (1 + Taxa Base + Spread) ^ Prazo` implementada com `BigDecimal` (não `double`/`float`), dado o contexto financeiro onde erro de arredondamento é inaceitável.
- **Geração de massa de dados / seed**: seed inicial de moedas (BRL/USD) e taxa de câmbio no `V1__init_schema.sql`, para permitir testar a aplicação localmente sem precisar cadastrar dados manualmente antes do primeiro uso.
- **Refatoração de queries**: pedido explícito de que o relatório de "Extrato de Liquidação" usasse SQL nativo via `JdbcTemplate` em vez de JPA puro, conforme diferencial pedido no enunciado, com filtros combináveis (cedente + moeda + período) construídos dinamicamente em `StringBuilder`.
- **Testes unitários**: prompt pedindo cobertura específica das regras de negócio mais sensíveis a erro silencioso — spread diferente por tipo de recebível, conversão cross-currency, e rejeição de data de vencimento no passado — em vez de testes genéricos de "getter/setter".

## Pontos onde a IA exigiu correção ou atenção redobrada

- **Exponenciação com expoente fracionário em `BigDecimal`**: `BigDecimal` não possui `pow()` nativo para expoentes não inteiros (necessário aqui, já que o prazo em meses é fracionário — ex: 47 dias = 1,5666 meses). A solução adotada (`Math.pow` sobre os valores convertidos para `double`, seguido de conversão de volta para `BigDecimal`) é uma concessão de precisão que **deve ser revisada** para um ambiente de produção real — o ideal seria uma biblioteca de precisão arbitrária (ex: `BigDecimalMath` do pacote `ch.obermuhlner:big-math`) para eliminar completamente o uso de `double` na cadeia de cálculo financeiro. Isso está documentado como uma decisão consciente, não um descuido.
- **Otimistic locking vs. requisito "Sênior"**: o enunciado lista "Optimistic Locking para evitar conflito de liquidação" como item de nível Sênior. Optei por implementá-lo mesmo no escopo Pleno porque o custo de adicionar `@Version` é baixo e o risco de race condition em liquidação financeira é uma preocupação legítima de qualquer nível — mas vale a pena deixar claro na entrevista que isso foi uma decisão deliberada de ir além do escopo mínimo, não um mal-entendido do enunciado.
- **CORS hardcoded para `localhost:4200`**: gerado como valor fixo em `CorsConfig`. Em um ambiente real, isso precisaria vir de configuração externa por ambiente (dev/staging/prod).

## Onde a IA economizou tempo

- Boilerplate de configuração (Spring Boot, Flyway, OpenAPI, Docker multi-stage builds, Angular standalone components) — código mecânico e bem documentado, onde o risco de erro humano por digitação/esquecimento é maior que o risco de a IA "inventar" algo incorreto.
- Geração paralela de DTOs de request/response com Bean Validation, evitando esquecer validações óbvias (`@NotBlank`, `@DecimalMin`, `@Future`) em algum campo.

## Onde a IA atrapalhou ou exigiu mais cuidado

- Código gerado por IA tende a "over-engineer" abstrações (ex: interfaces genéricas demais) quando não guiado com um escopo claro. Foi necessário podar algumas sugestões iniciais para manter o projeto no tamanho e na complexidade adequados a um teste técnico de 3-4 dias, e não a um sistema de produção completo.
- Como o ambiente de geração não tinha acesso ao Maven Central / npm registry para compilar e rodar os testes durante a construção, **é essencial rodar `mvn test` e `npm run build` localmente antes da entrega** para capturar qualquer erro de compilação que não pôde ser validado durante a geração.

## Análise crítica

A IA foi usada como acelerador de escrita e como "par de revisão" para não esquecer requisitos do enunciado (ex: ACID, Strategy Pattern, SQL nativo em relatórios), mas todas as decisões de arquitetura (separação de camadas, onde aplicar Strategy, quando usar SQL nativo vs. JPA, o que logar, como versionar o schema) foram guiadas explicitamente por mim via prompt, e não aceitas às cegas. O objetivo foi usar a IA para potencializar a velocidade de entrega sem abrir mão de entender e conseguir defender cada linha do código nesta entrega.
