# ADR-001: Clean Architecture

## Status

**Aceita** - 2024-01-15

## Contexto

O microserviço OS Service foi extraído de um monolito e precisa de uma arquitetura que:

- Facilite a manutenção e evolução do código
- Permita substituição de componentes de infraestrutura sem impactar regras de negócio
- Facilite testes unitários isolados das dependências externas
- Siga boas práticas de engenharia de software

## Decisão

Adotamos a **Clean Architecture** (Arquitetura Limpa) proposta por Robert C. Martin, organizando o código em camadas concêntricas com dependências apontando para dentro.

### Estrutura de Camadas

```
┌─────────────────────────────────────────────────────────────┐
│                    Infrastructure Layer                      │
│  - Controllers REST                                          │
│  - Repositórios JPA                                          │
│  - Mensageria AWS SQS                                        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                         │
│  - Use Cases (casos de uso)                                  │
│  - Gateways (interfaces para infraestrutura)                 │
│  - Presenters (formatação de respostas)                      │
│  - DTOs (objetos de transferência de dados)                  │
│  - Entities (objetos de domínio)                             │
│  - Exceptions (exceções de negócio)                          │
└─────────────────────────────────────────────────────────────┘
```

### Organização de Pacotes

```
com.techchallenge.fiap.cargarage.os_service/
├── application/
│   ├── controller/      # (deveria estar em infrastructure, mas mantido por convenção)
│   ├── dto/             # Data Transfer Objects
│   ├── entity/          # Entidades de domínio (Value Objects, Aggregates)
│   ├── enums/           # Enumerações de domínio
│   ├── exception/       # Exceções de negócio
│   ├── gateway/         # Interfaces para infraestrutura
│   ├── interfaces/      # Interfaces adicionais
│   ├── presenter/       # Formatadores de resposta
│   └── usecase/         # Casos de uso
├── configuration/       # Configurações Spring
└── infrastructure/
    ├── controller/      # Controllers REST
    ├── database/        # Implementações JPA
    └── messaging/       # AWS SQS Publishers/Listeners
```

### Regras Principais

1. **Entidades** não dependem de nada externo
2. **Use Cases** dependem apenas de Entities e Gateways (interfaces)
3. **Infrastructure** implementa os Gateways
4. **Inversão de Dependência** através de interfaces (Gateways)

## Consequências

### Positivas

- ✅ **Testabilidade**: Use cases podem ser testados isoladamente com mocks dos gateways
- ✅ **Manutenibilidade**: Mudanças em infraestrutura não afetam regras de negócio
- ✅ **Flexibilidade**: Fácil trocar banco de dados, message broker, etc.
- ✅ **Clareza**: Separação clara de responsabilidades
- ✅ **Independência de Framework**: Lógica de negócio não depende do Spring

### Negativas

- ❌ **Complexidade inicial**: Mais arquivos e indireções
- ❌ **Curva de aprendizado**: Desenvolvedores precisam entender o padrão
- ❌ **Boilerplate**: DTOs, Gateways e implementações adicionam código

## Alternativas Consideradas

1. **MVC Tradicional**: Rejeitado por acoplar lógica de negócio aos controllers
2. **Hexagonal Architecture**: Similar, mas Clean Architecture é mais conhecida na equipe
3. **DDD Layered**: Considerado, mas Clean Architecture oferece mais isolamento

## Referências

- [Clean Architecture - Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Clean Architecture Book](https://www.amazon.com/Clean-Architecture-Craftsmans-Software-Structure/dp/0134494164)
