# ADR-004: LocalStack para Desenvolvimento Local

## Status

**Aceita** - 2024-02-01

## Contexto

Com a adoção de AWS SQS para messaging (ADR-002), precisamos de uma forma de:

- Desenvolver e testar localmente sem depender de recursos AWS reais
- Evitar custos de desenvolvimento em ambiente cloud
- Permitir execução offline
- Manter paridade com ambiente de produção

## Decisão

Adotamos **LocalStack** como emulador de serviços AWS para desenvolvimento local.

### Configuração Docker Compose

```yaml
services:
  localstack:
    image: localstack/localstack:3.4
    ports:
      - "4566:4566"           # Porta padrão LocalStack
    environment:
      - SERVICES=sqs          # Habilitamos apenas SQS
      - DEBUG=1
      - AWS_DEFAULT_REGION=us-east-1
    volumes:
      - "./localstack/init-aws.sh:/etc/localstack/init/ready.d/init-aws.sh"
```

### Script de Inicialização

```bash
#!/bin/bash
# localstack/init-aws.sh

# Criar filas SQS
awslocal sqs create-queue --queue-name os-service-status-changed
awslocal sqs create-queue --queue-name quote-approved
awslocal sqs create-queue --queue-name execution-completed
awslocal sqs create-queue --queue-name payment-failed
awslocal sqs create-queue --queue-name resource-unavailable

echo "LocalStack SQS queues created successfully!"
```

### Configuração da Aplicação

```properties
# application.properties (local)
spring.cloud.aws.sqs.endpoint=http://localhost:4566
spring.cloud.aws.sqs.region=us-east-1
spring.cloud.aws.credentials.access-key=test
spring.cloud.aws.credentials.secret-key=test
```

### Execução

```bash
# Iniciar LocalStack
docker-compose up -d localstack

# Verificar filas criadas
aws --endpoint-url=http://localhost:4566 sqs list-queues

# Iniciar aplicação
./mvnw spring-boot:run
```

## Consequências

### Positivas

- ✅ **Desenvolvimento Offline**: Funciona sem internet
- ✅ **Zero Custo**: Sem gastos com AWS durante desenvolvimento
- ✅ **Paridade**: API compatível com AWS real
- ✅ **Reprodutibilidade**: Ambiente idêntico para todos os devs
- ✅ **CI/CD**: Pode ser usado em pipelines de teste

### Negativas

- ❌ **Não é 100% AWS**: Pode haver diferenças sutis de comportamento
- ❌ **Performance**: Mais lento que SQS real em alguns casos
- ❌ **Manutenção**: Precisa manter scripts de inicialização atualizados

## Alternativas Consideradas

### 1. AWS Real com Perfil de Dev

**Prós**: 100% compatível

**Contras**: Custo, requer internet, complexidade de IAM

**Decisão**: Rejeitado - não convém para desenvolvimento diário

### 2. Mocks in-memory

**Prós**: Simples, rápido

**Contras**: Não testa integração real, comportamento diferente

**Decisão**: Usado apenas em testes unitários, não substitui LocalStack

### 3. ElasticMQ (SQS-compatible)

**Prós**: Leve, focado em SQS

**Contras**: Menos popular, menos suporte

**Decisão**: Rejeitado - LocalStack é mais completo e conhecido

## Estrutura de Diretórios

```
fiap-techchallenge-microservice-os-service/
├── docker-compose.yaml
├── localstack/
│   └── init-aws.sh          # Script de criação de recursos
├── app/
│   └── src/main/resources/
│       └── application.properties
```

## Perfis de Ambiente

| Ambiente | Endpoint SQS | Credenciais |
|----------|--------------|-------------|
| Local | `http://localhost:4566` | `test/test` |
| Dev | `https://sqs.us-east-1.amazonaws.com` | IAM Role |
| Prod | `https://sqs.us-east-1.amazonaws.com` | IAM Role |

## Referências

- [LocalStack Documentation](https://docs.localstack.cloud/)
- [LocalStack SQS](https://docs.localstack.cloud/user-guide/aws/sqs/)
