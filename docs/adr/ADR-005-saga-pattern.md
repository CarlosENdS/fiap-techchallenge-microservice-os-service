# ADR-005: Saga Pattern para Transações Distribuídas

## Status

**Aceita** - 2024-02-05

## Contexto

O OS Service é um microserviço que faz parte de uma arquitetura distribuída. Uma ordem de serviço envolve:

- **Pagamento**: Processado por um serviço de pagamentos
- **Estoque/Recursos**: Gerenciado por um serviço de recursos
- **Execução**: Coordenada pelo OS Service

Precisamos garantir consistência eventual quando operações falham em serviços distribuídos, já que transações ACID tradicionais não funcionam entre microserviços.

## Decisão

Adotamos o **Saga Pattern** com **Coreografia** (Choreography) para coordenar transações distribuídas através de eventos assíncronos via AWS SQS.

### Tipo de Saga: Coreografia

Na coreografia, cada serviço:
1. Executa sua transação local
2. Publica evento de conclusão ou falha
3. Escuta eventos de outros serviços para reagir

**Razão da escolha sobre Orquestração**: Menor acoplamento, sem ponto único de falha, mais simples para nosso caso de uso.

### Fluxo Normal (Happy Path)

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  OS Service │     │  Resource   │     │  Payment    │     │  OS Service │
│  (criar OS) │     │   Service   │     │   Service   │     │  (finalizar)│
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │                   │
       │ OS_CREATED        │                   │                   │
       │──────────────────▶│                   │                   │
       │                   │ RESOURCE_RESERVED │                   │
       │                   │──────────────────▶│                   │
       │                   │                   │ PAYMENT_COMPLETED │
       │                   │                   │──────────────────▶│
       │                   │                   │                   │ EXECUTION_COMPLETED
       │◀──────────────────────────────────────────────────────────│
       │                   │                   │                   │
```

### Fluxo de Compensação (Failure Path)

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  OS Service │     │  Payment    │     │  Resource   │
│             │     │   Service   │     │   Service   │
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │
       │                   │ PAYMENT_FAILED    │
       │◀──────────────────│                   │
       │                   │                   │
       │ (muda status para │                   │
       │  IN_DIAGNOSIS)    │ RELEASE_RESOURCES │
       │──────────────────────────────────────▶│
       │                   │                   │
```

### Eventos de Compensação Tratados

| Evento | Ação de Compensação |
|--------|---------------------|
| `payment-failed` | Retorna OS para `IN_DIAGNOSIS` |
| `resource-unavailable` | Retorna OS para `IN_DIAGNOSIS` |
| `execution-completed` | Avança OS para `FINISHED` |
| `quote-approved` | Avança OS para `IN_EXECUTION` |

### Implementação

```java
@Component
public class SqsEventListener {
    
    @Autowired
    private ProcessCompensationUseCase compensationUseCase;
    
    @SqsListener("${messaging.sqs.queue.payment-failed}")
    public void onPaymentFailed(PaymentFailedEvent event) {
        // Compensação: retorna para diagnóstico
        compensationUseCase.execute(
            event.getOrderId(), 
            CompensationType.PAYMENT_FAILED
        );
    }
    
    @SqsListener("${messaging.sqs.queue.resource-unavailable}") 
    public void onResourceUnavailable(ResourceUnavailableEvent event) {
        // Compensação: retorna para diagnóstico
        compensationUseCase.execute(
            event.getOrderId(),
            CompensationType.RESOURCE_UNAVAILABLE
        );
    }
}
```

## Consequências

### Positivas

- ✅ **Desacoplamento**: Serviços não se conhecem diretamente
- ✅ **Resiliência**: Falha em um serviço não bloqueia outros
- ✅ **Escalabilidade**: Cada serviço escala independentemente
- ✅ **Consistência Eventual**: Sistema converge para estado consistente
- ✅ **Auditabilidade**: Eventos fornecem log de auditoria natural

### Negativas

- ❌ **Complexidade**: Debug mais difícil que transações locais
- ❌ **Consistência Eventual**: Janela de inconsistência temporária
- ❌ **Ordenação**: Eventos podem chegar fora de ordem
- ❌ **Idempotência**: Todos handlers precisam ser idempotentes

## Alternativas Consideradas

### 1. Two-Phase Commit (2PC)

**Prós**: Consistência forte

**Contras**: Bloqueante, não escala, ponto único de falha (coordenador)

**Decisão**: Rejeitado - não funciona bem em microserviços

### 2. Saga com Orquestração

**Prós**: Fluxo centralizado, mais fácil de entender

**Contras**: Orquestrador é ponto único de falha, mais acoplamento

**Decisão**: Rejeitado - coreografia é mais resiliente para nosso caso

### 3. Outbox Pattern + CDC

**Prós**: Garantia de entrega, atomicidade com banco

**Contras**: Complexidade adicional, requer Debezium ou similar

**Decisão**: Considerado para futuro, mas SQS+DLQ atende agora

## Garantias de Entrega

1. **At-least-once**: SQS garante entrega pelo menos uma vez
2. **Dead Letter Queue**: Mensagens com falha vão para DLQ após N tentativas
3. **Idempotência**: Handlers verificam estado atual antes de agir

## Referências

- [Saga Pattern - Microsoft](https://docs.microsoft.com/en-us/azure/architecture/reference-architectures/saga/saga)
- [Microservices Patterns - Chris Richardson](https://microservices.io/patterns/data/saga.html)
