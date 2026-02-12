# OS Service Microservice

Microserviço para gerenciamento de Ordens de Serviço (OS) do sistema Car Garage.

## 📋 Sumário

- [Visão Geral](#-visão-geral)
- [Arquitetura](#-arquitetura)
- [Tecnologias](#-tecnologias)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Workflow de Status](#-workflow-de-status)
- [Saga Pattern](#-saga-pattern)
- [API Endpoints](#-api-endpoints)
- [Execução Local](#-execução-local)
- [Docker](#-docker)
- [Kubernetes](#-kubernetes)
- [Testes](#-testes)
- [CI/CD](#-cicd)
- [Documentação da API](#-documentação-da-api)

## 🎯 Visão Geral

O OS Service é um microserviço extraído do monolito Car Garage, responsável pelo gerenciamento completo do ciclo de vida das Ordens de Serviço. Implementa arquitetura limpa (Clean Architecture), comunicação assíncrona via **AWS SQS** para orquestração de Saga Pattern e está preparado para deploy em ambiente **AWS com EKS** e **RDS PostgreSQL**.

### Funcionalidades Principais

- ✅ Criação e gerenciamento de ordens de serviço
- ✅ Controle do fluxo de trabalho (workflow de status)
- ✅ Aprovação/rejeição de orçamentos pelo cliente
- ✅ Cálculo de tempo de execução
- ✅ Compensação de transações distribuídas (Saga Pattern)
- ✅ API RESTful documentada com OpenAPI/Swagger
- ✅ Métricas e health checks para observabilidade

## 🏗 Arquitetura

### Clean Architecture

O projeto segue os princípios da Arquitetura Limpa, separando responsabilidades em camadas:

```
┌─────────────────────────────────────────────────────────────┐
│                    Infrastructure Layer                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │  Controller  │  │   Database   │  │    Messaging     │   │
│  │    (REST)    │  │  (JPA/SQL)   │  │   (AWS SQS)      │   │
│  └──────────────┘  └──────────────┘  └──────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │  Use Cases   │  │   Gateway    │  │    Presenter     │   │
│  │              │  │              │  │                  │   │
│  └──────────────┘  └──────────────┘  └──────────────────┘   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │   Entities   │  │     DTOs     │  │   Exceptions     │   │
│  │   (Domain)   │  │              │  │                  │   │
│  └──────────────┘  └──────────────┘  └──────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Diagrama de Componentes

```
┌─────────────────────────────────────────────────────────────────────┐
│                         OS Service Microservice                      │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│   ┌───────────────┐     ┌───────────────┐     ┌───────────────┐     │
│   │  REST API     │────▶│  Use Cases    │────▶│   Gateway     │     │
│   │  Controller   │     │               │     │               │     │
│   └───────────────┘     └───────────────┘     └───────────────┘     │
│          │                     │                      │              │
│          │                     ▼                      ▼              │
│          │              ┌─────────────┐        ┌────────────┐       │
│          │              │   Event     │        │ DataSource │       │
│          │              │  Publisher  │        │            │       │
│          │              └─────────────┘        └────────────┘       │
│          │                     │                      │              │
└──────────│─────────────────────│──────────────────────│──────────────┘
           │                     │                      │
           ▼                     ▼                      ▼
     ┌──────────┐         ┌──────────┐          ┌──────────┐
     │  Client  │         │ AWS SQS  │          │ AWS RDS  │
     │          │         │          │          │PostgreSQL│
     └──────────┘         └──────────┘          └──────────┘
```

## 🛠 Tecnologias

| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| Java | 21 | Linguagem de programação |
| Spring Boot | 3.4.7 | Framework principal |
| Spring Data JPA | - | Persistência de dados |
| PostgreSQL | 16 | Banco de dados |
| AWS SQS | - | Message broker (Filas na AWS) |
| LocalStack | 3.4 | Emulador AWS para testes locais |
| Maven | 3.9+ | Gerenciador de dependências |
| JUnit 5 | - | Framework de testes |
| Cucumber | 7.18.0 | BDD testing |
| JaCoCo | 0.8.12 | Cobertura de código |
| Lombok | - | Redução de boilerplate |
| SpringDoc OpenAPI | 2.8.9 | Documentação da API |
| Docker | - | Containerização |
| Kubernetes | 1.25+ | Orquestração |

## 📁 Estrutura do Projeto

```
fiap-techchallenge-microservice-os-service/
├── .github/
│   └── workflows/
│       ├── ci.yml                    # Pipeline de CI
│       └── cd.yml                    # Pipeline de CD
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/.../os_service/
│   │   │   │   ├── application/      # Camada de aplicação
│   │   │   │   │   ├── controller/   # Controllers Clean Arch
│   │   │   │   │   ├── dto/          # Data Transfer Objects
│   │   │   │   │   ├── entity/       # Entidades de domínio
│   │   │   │   │   ├── enums/        # Enumerações
│   │   │   │   │   ├── exception/    # Exceções de negócio
│   │   │   │   │   ├── gateway/      # Gateways
│   │   │   │   │   ├── interfaces/   # Interfaces/contratos
│   │   │   │   │   ├── presenter/    # Presenters
│   │   │   │   │   └── usecase/      # Casos de uso
│   │   │   │   ├── configuration/    # Configurações Spring
│   │   │   │   └── infrastructure/   # Camada de infraestrutura
│   │   │   │       ├── controller/   # REST Controllers
│   │   │   │       ├── database/     # Entidades JPA e Repository
│   │   │   │       └── messaging/    # AWS SQS
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   │       ├── java/.../os_service/
│   │       │   ├── application/      # Testes unitários
│   │       │   ├── bdd/              # Testes BDD (Cucumber)
│   │       │   └── infrastructure/   # Testes de integração
│   │       └── resources/
│   │           ├── features/         # Arquivos .feature
│   │           └── application-test.properties
│   ├── Dockerfile
│   └── pom.xml
├── database/
│   └── init-scripts/                 # Scripts de inicialização
├── k8s/                              # Manifestos Kubernetes
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── secrets.yaml
│   ├── postgres-deployment.yaml
│   ├── service-account.yaml         # Service Account para IRSA
│   ├── app-deployment.yaml
│   ├── app-service.yaml
│   ├── hpa.yaml
│   └── README.md
├── docker-compose.yaml
└── README.md
```

## 🔄 Workflow de Status

A Ordem de Serviço segue um fluxo de estados bem definido:

```
┌──────────┐     ┌──────────────┐     ┌───────────────────┐
│ RECEIVED │────▶│ IN_DIAGNOSIS │────▶│ WAITING_APPROVAL  │
└──────────┘     └──────────────┘     └───────────────────┘
                                               │
                          ┌────────────────────┼────────────────────┐
                          │ (Aprovado)         │                    │ (Rejeitado)
                          ▼                    │                    ▼
                   ┌──────────────┐            │             ┌───────────┐
                   │ IN_EXECUTION │            │             │ CANCELLED │
                   └──────────────┘            │             └───────────┘
                          │                    │
                          ▼                    │
                   ┌──────────┐                │
                   │ FINISHED │                │
                   └──────────┘                │
                          │                    │
                          ▼                    │
                   ┌───────────┐               │
                   │ DELIVERED │               │
                   └───────────┘               │
                                               │
    ┌──────────────────────────────────────────┘
    │ (Cancelamento possível antes de FINISHED)
    ▼
┌───────────┐
│ CANCELLED │
└───────────┘
```

### Estados

| Status | Descrição |
|--------|-----------|
| `RECEIVED` | OS recebida, aguardando diagnóstico |
| `IN_DIAGNOSIS` | Em análise pelo mecânico |
| `WAITING_APPROVAL` | Orçamento gerado, aguardando aprovação do cliente |
| `IN_EXECUTION` | Serviço em execução |
| `FINISHED` | Serviço finalizado |
| `DELIVERED` | Veículo entregue ao cliente |
| `CANCELLED` | OS cancelada |

## 🔀 Saga Pattern

O microserviço implementa o padrão Saga para garantir consistência em transações distribuídas.

### Eventos Publicados

Os eventos são publicados na fila FIFO `os-order-events-queue.fifo` no AWS SQS:

| Evento | Descrição |
|--------|-----------|
| ORDER_CREATED | Nova OS criada |
| ORDER_WAITING_APPROVAL | Orçamento aguardando aprovação |
| ORDER_APPROVED | Orçamento aprovado pelo cliente |
| ORDER_REJECTED | Orçamento rejeitado pelo cliente |
| ORDER_FINISHED | Serviço finalizado |
| ORDER_DELIVERED | Veículo entregue |
| ORDER_CANCELLED | OS cancelada |

### Eventos Consumidos (Compensação)

| Evento | Queue | Ação |
|--------|-------|------|
| Quote Approved | `quote-approved-queue` | Inicia execução do serviço |
| Execution Completed | `execution-completed-queue` | Marca serviço como finalizado |
| Payment Failed | `payment-failed-queue` | Cancela a OS |
| Resource Unavailable | `resource-unavailable-queue` | Cancela a OS |

### Diagrama de Sequência - Saga

```
┌────────┐      ┌────────────┐      ┌──────────┐      ┌─────────┐
│ Client │      │ OS Service │      │ AWS SQS  │      │ Billing │
└────┬───┘      └──────┬─────┘      └────┬─────┘      └────┬────┘
     │                 │                 │                 │
     │ Create OS       │                 │                 │
     │────────────────▶│                 │                 │
     │                 │                 │                 │
     │                 │ ORDER_CREATED   │                 │
     │                 │────────────────▶│                 │
     │                 │                 │                 │
     │ Approve         │                 │                 │
     │────────────────▶│                 │                 │
     │                 │                 │                 │
     │                 │ ORDER_APPROVED  │                 │
     │                 │────────────────▶│────────────────▶│
     │                 │                 │                 │
     │                 │                 │  payment.failed │
     │                 │◀────────────────│◀────────────────│
     │                 │                 │                 │
     │                 │ Cancel OS       │                 │
     │                 │ (Compensation)  │                 │
     │                 │                 │                 │
     │                 │ ORDER_CANCELLED │                 │
     │                 │────────────────▶│                 │
```

## 📡 API Endpoints

### Service Orders

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/api/v1/service-orders` | Criar nova OS |
| `GET` | `/api/v1/service-orders` | Listar todas as OS |
| `GET` | `/api/v1/service-orders/{id}` | Obter OS por ID |
| `PUT` | `/api/v1/service-orders/{id}` | Atualizar OS |
| `PATCH` | `/api/v1/service-orders/{id}/status` | Atualizar status |
| `POST` | `/api/v1/service-orders/{id}/approval` | Processar aprovação |
| `DELETE` | `/api/v1/service-orders/{id}` | Cancelar OS |
| `GET` | `/api/v1/service-orders/{id}/execution-time` | Obter tempo de execução |
| `GET` | `/api/v1/service-orders/customer/{customerId}` | OS por cliente |
| `GET` | `/api/v1/service-orders/vehicle/{vehicleId}` | OS por veículo |
| `GET` | `/api/v1/service-orders/status/{status}` | OS por status |

### Actuator

| Endpoint | Descrição |
|----------|-----------|
| `/actuator/health` | Health check |
| `/actuator/info` | Informações da aplicação |
| `/actuator/metrics` | Métricas |
| `/actuator/prometheus` | Métricas Prometheus |

## 🚀 Execução Local

### Pré-requisitos

- Java 21
- Maven 3.9+
- Docker (para LocalStack e PostgreSQL)

### Usando Docker Compose (Recomendado para testes locais)

```bash
# Inicia PostgreSQL + LocalStack (emula AWS SQS)
docker-compose up -d
```

O LocalStack criará automaticamente as filas SQS necessárias:
- `os-order-events-queue.fifo` (FIFO para eventos de saída)
- `quote-approved-queue`
- `execution-completed-queue`
- `payment-failed-queue`
- `resource-unavailable-queue`

### Configuração

1. Clone o repositório:
```bash
git clone https://github.com/fiap/fiap-techchallenge-microservice-os-service.git
cd fiap-techchallenge-microservice-os-service
```

2. Para execução local com LocalStack, use o perfil `local`:
```bash
# As configurações estão em application-local.properties
export SPRING_PROFILES_ACTIVE=local
```

Ou configure as variáveis para AWS:
```properties
# Banco de dados (AWS RDS)
DB_URL=jdbc:postgresql://your-rds-endpoint.rds.amazonaws.com:5432/os_service_db
DB_USERNAME=os_service_user
DB_PASSWORD=os_service_password

# AWS SQS
AWS_REGION=us-east-1
SQS_OS_EVENTS_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/123456789012/os-order-events-queue.fifo
```

3. Execute a aplicação:
```bash
cd app
./mvnw spring-boot:run
```

4. Acesse:
- API: http://localhost:8081
- Swagger UI: http://localhost:8081/swagger-ui.html
- Health Check: http://localhost:8081/actuator/health

## 🐳 Docker

### Build da Imagem

```bash
cd app
docker build -t os-service:latest .
```

### Docker Compose

Execute toda a stack:

```bash
docker-compose up -d
```

Serviços disponíveis:
- OS Service: http://localhost:8081
- PostgreSQL: localhost:5433
- LocalStack (AWS SQS): http://localhost:4566

### Verificando as filas SQS no LocalStack

```bash
# Listar filas
aws --endpoint-url=http://localhost:4566 sqs list-queues

# Ver mensagens em uma fila
aws --endpoint-url=http://localhost:4566 sqs receive-message \
  --queue-url http://localhost:4566/000000000000/os-order-events-queue.fifo
```

### Parar os serviços

```bash
docker-compose down
docker-compose down -v  # Remove volumes
```

## ☸️ Kubernetes

### Deploy

```bash
# Criar namespace
kubectl apply -f k8s/namespace.yaml

# Aplicar todos os recursos
kubectl apply -f k8s/

# Verificar deployment
kubectl get all -n os-service
```

Consulte [k8s/README.md](k8s/README.md) para instruções detalhadas.

## 🧪 Testes

### Executar Todos os Testes

```bash
cd app
./mvnw verify
```

### Testes Unitários

```bash
./mvnw test
```

### Testes BDD (Cucumber)

```bash
./mvnw test -Dcucumber.filter.tags="not @ignore"
```

### Cobertura de Código

```bash
./mvnw jacoco:report
```

Relatório disponível em: `app/target/site/jacoco/index.html`

**Cobertura mínima exigida: 80%**

## 🔄 CI/CD

### CI Pipeline (ci.yml)

Executado em push/PR para `main`, `develop`, `feature/**`:

1. Build e compilação
2. Testes unitários
3. Testes de integração
4. Verificação de cobertura (JaCoCo)
5. Análise de código (Checkstyle, SpotBugs)
6. Scan de segurança (OWASP)
7. Build da imagem Docker
8. Testes BDD

### CD Pipeline (cd.yml)

Executado em push para `main` ou tags `v*`:

1. Build da imagem Docker
2. Push para Container Registry
3. Deploy em Staging
4. Deploy em Production (para tags)
5. Rollback automático em caso de falha

## 📖 Documentação da API

### Swagger UI

Acesse a documentação interativa:

```
http://localhost:8081/swagger-ui.html
```

### OpenAPI Spec

```
http://localhost:8081/api-docs
```

## 📄 Licença

Este projeto está sob a licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 👥 Equipe

FIAP Tech Challenge - Fase 4

---

**Versão:** 1.0.0  
**Java:** 21  
**Spring Boot:** 3.4.7
