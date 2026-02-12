# ADR-003: State Machine para Workflow de Status

## Status

**Aceita** - 2024-01-20

## Contexto

Ordens de serviço passam por um ciclo de vida complexo com múltiplos estados e transições válidas. Precisamos:

- Garantir que apenas transições válidas sejam permitidas
- Evitar estados inconsistentes
- Facilitar a compreensão do fluxo de negócio
- Rastrear datas de cada etapa do ciclo de vida

## Decisão

Implementamos um **State Machine** (Máquina de Estados) através do Value Object `ServiceOrderStatus` que encapsula a lógica de transições válidas.

### Estados Definidos

| Status | Descrição |
|--------|-----------|
| `RECEIVED` | Ordem recebida, aguardando diagnóstico |
| `IN_DIAGNOSIS` | Em análise pelo técnico |
| `WAITING_APPROVAL` | Aguardando aprovação do orçamento pelo cliente |
| `IN_EXECUTION` | Serviço em execução |
| `FINISHED` | Serviço concluído |
| `DELIVERED` | Veículo entregue ao cliente |
| `CANCELLED` | Ordem cancelada |

### Diagrama de Transições

```
                    ┌─────────────────────────────────────────────┐
                    │                                             │
                    ▼                                             │
┌──────────┐    ┌──────────────┐    ┌───────────────────┐    ┌────┴──────┐
│ RECEIVED │───▶│ IN_DIAGNOSIS │───▶│ WAITING_APPROVAL  │───▶│CANCELLED  │
└──────────┘    └──────────────┘    └───────────────────┘    └───────────┘
     │                 ▲                    │      │
     │                 │                    │      │
     │                 └────────────────────┘      │
     │              (rejeição retorna p/ diagnóstico)
     │                                             │
     └─────────────────────────────────────────────┘
                                                   
┌───────────────────┐    ┌──────────┐    ┌───────────┐
│ WAITING_APPROVAL  │───▶│IN_EXECUTION│──▶│ FINISHED  │───▶│ DELIVERED │
│   (aprovado)      │    └──────────┘    └───────────┘    └───────────┘
└───────────────────┘
```

### Regras de Transição

```java
public boolean canTransitionTo(ServiceOrderStatus targetStatus) {
    switch (currentStatus) {
        case RECEIVED:
            return target == IN_DIAGNOSIS || target == CANCELLED;
        case IN_DIAGNOSIS:
            return target == WAITING_APPROVAL || target == CANCELLED;
        case WAITING_APPROVAL:
            return target == IN_EXECUTION || target == IN_DIAGNOSIS || target == CANCELLED;
        case IN_EXECUTION:
            return target == FINISHED;  // Não pode cancelar em execução!
        case FINISHED:
            return target == DELIVERED;
        case DELIVERED:
        case CANCELLED:
            return false;  // Estados finais
    }
}
```

### Regras de Negócio Importantes

1. **Rejeição ≠ Cancelamento**: Quando cliente rejeita o orçamento, a ordem volta para `IN_DIAGNOSIS` para revisão, não é cancelada
2. **Em Execução**: Não pode ser cancelada (trabalho já iniciado, custos incorridos)
3. **Estados Finais**: `DELIVERED` e `CANCELLED` são estados terminais
4. **Rastreamento de Datas**: Cada transição grava a data correspondente na entidade

### Implementação como Value Object

```java
@EqualsAndHashCode
public final class ServiceOrderStatus {
    private final String status;
    
    // Factory methods para cada status
    public static ServiceOrderStatus received() { ... }
    public static ServiceOrderStatus inDiagnosis() { ... }
    
    // Predicados para verificação
    public boolean isReceived() { ... }
    public boolean isInExecution() { ... }
    
    // Validação de transição
    public boolean canTransitionTo(ServiceOrderStatus target) { ... }
}
```

## Consequências

### Positivas

- ✅ **Validação Centralizada**: Lógica de transição em um único lugar
- ✅ **Type Safety**: Impossível criar status inválido
- ✅ **Testabilidade**: Fácil testar todas as transições
- ✅ **Documentação Viva**: Código é a documentação
- ✅ **Imutabilidade**: Value Object imutável garante consistência

### Negativas

- ❌ **Rigidez**: Mudanças no workflow exigem alteração de código
- ❌ **Distribuição**: Se múltiplos serviços precisarem das mesmas regras, duplicação

## Alternativas Consideradas

### 1. Spring State Machine

**Prós**: Framework completo, suporte a eventos, persistência

**Contras**: Complexidade excessiva para nosso caso, curva de aprendizado

**Decisão**: Rejeitado - over-engineering

### 2. Enum com Transições

**Prós**: Simples, compile-time safety

**Contras**: Menos flexível que Value Object

**Decisão**: Usamos enum para validação, mas Value Object para representação

### 3. Regras em Banco de Dados

**Prós**: Flexível, não requer deploy

**Contras**: Complexo, difícil de testar, performance

**Decisão**: Rejeitado - regras de negócio devem estar no código

## Referências

- [State Pattern - GoF](https://refactoring.guru/design-patterns/state)
- [Value Objects - DDD](https://martinfowler.com/bliki/ValueObject.html)
