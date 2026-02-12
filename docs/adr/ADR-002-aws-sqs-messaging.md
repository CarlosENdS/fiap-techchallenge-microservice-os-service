# ADR-002: AWS SQS para Messaging

## Status

**Aceita** - 2024-02-01

## Contexto

O microserviço OS Service precisa de comunicação assíncrona com outros serviços para:

- Publicar eventos de mudança de status das ordens de serviço
- Receber eventos de compensação do Saga Pattern (pagamento falhou, recurso indisponível, etc.)
- Garantir entrega confiável de mensagens mesmo em caso de falhas
- Suportar escalabilidade horizontal

A infraestrutura alvo é **AWS com EKS** (Kubernetes), e precisamos escolher uma solução de mensageria.

## Decisão

Adotamos **AWS SQS (Simple Queue Service)** como message broker, utilizando a biblioteca **Spring Cloud AWS** para integração.

### Filas Configuradas

| Fila | Direção | Propósito |
|------|---------|-----------|
| `os-service-status-changed` | Outbound | Notifica mudanças de status |
| `quote-approved` | Inbound | Recebe aprovação de orçamento |
| `execution-completed` | Inbound | Recebe conclusão de execução |
| `payment-failed` | Inbound | Recebe falha de pagamento (compensação) |
| `resource-unavailable` | Inbound | Recebe indisponibilidade de recurso (compensação) |

### Implementação

```java
// Publisher - Publicação de eventos
@Component
public class SqsEventPublisher implements ServiceOrderEventPublisher {
    
    @Autowired
    private SqsTemplate sqsTemplate;
    
    public void publish(ServiceOrderEventDto event) {
        sqsTemplate.send(queueUrl, event);
    }
}

// Listener - Consumo de eventos
@Component
public class SqsEventListener {
    
    @SqsListener("${messaging.sqs.queue.quote-approved}")
    public void onQuoteApproved(String message) {
        // Process approval
    }
}
```

### Configuração

```yaml
# application.properties
spring.cloud.aws.sqs.region=us-east-1
spring.cloud.aws.credentials.access-key=${AWS_ACCESS_KEY}
spring.cloud.aws.credentials.secret-key=${AWS_SECRET_KEY}

messaging.sqs.queue.status-changed=os-service-status-changed
messaging.sqs.queue.quote-approved=quote-approved
messaging.sqs.queue.execution-completed=execution-completed
messaging.sqs.queue.payment-failed=payment-failed
messaging.sqs.queue.resource-unavailable=resource-unavailable
```

## Consequências

### Positivas

- ✅ **Integração Nativa AWS**: Funciona nativamente com EKS e outros serviços AWS
- ✅ **Gerenciado**: Sem necessidade de gerenciar infraestrutura de broker
- ✅ **Escalável**: Suporta milhões de mensagens por segundo
- ✅ **Durável**: Mensagens persistidas com alta disponibilidade
- ✅ **Dead Letter Queue**: Suporte nativo para mensagens com falha
- ✅ **Segurança**: IAM roles para autenticação/autorização
- ✅ **Custo**: Pay-per-use, sem custos fixos de infraestrutura

### Negativas

- ❌ **Vendor Lock-in**: Acoplamento com AWS
- ❌ **Desenvolvimento Local**: Requer LocalStack ou mock
- ❌ **Latência**: Maior que brokers auto-hospedados (10-25ms)
- ❌ **Falta de Features Avançadas**: Sem routing complexo como RabbitMQ

## Alternativas Consideradas

### 1. RabbitMQ

**Prós**: Open source, features avançadas de routing, desarrollo local fácil

**Contras**: Requer gerenciamento de infraestrutura no EKS, mais complexidade operacional

**Decisão**: Rejeitado - custo operacional alto para equipe pequena

### 2. Apache Kafka

**Prós**: Alta throughput, event sourcing nativo, replay de mensagens

**Contras**: Complexo, requer ZooKeeper, over-engineering para nosso caso de uso

**Decisão**: Rejeitado - complexidade desnecessária

### 3. Amazon SNS + SQS

**Prós**: Pub/sub com fan-out

**Contras**: Complexidade adicional sem benefício claro

**Decisão**: Rejeitado - SQS puro atende os requisitos atuais

## Mitigações

### Vendor Lock-in

- Interface `ServiceOrderEventPublisher` abstrai a implementação
- Possível trocar para RabbitMQ ou Kafka alterando apenas `infrastructure/messaging`

### Desenvolvimento Local

- **LocalStack** simula SQS localmente (ver ADR-004)
- Testes usam perfil que desabilita auto-configuração AWS

## Referências

- [AWS SQS Documentation](https://docs.aws.amazon.com/sqs/)
- [Spring Cloud AWS SQS](https://docs.awspring.io/spring-cloud-aws/docs/3.0.0/reference/html/index.html#sqs-integration)
