# Contrato de Filas SQS

Este documento descreve os contratos **reais** usados pela aplicação para publicação e consumo de mensagens SQS.

## Configuração

As filas são configuradas em `application.properties` com as chaves:

- `messaging.sqs.queue.os-events-url`
- `messaging.sqs.queue.quote-approved`
- `messaging.sqs.queue.execution-completed`
- `messaging.sqs.queue.payment-failed`
- `messaging.sqs.queue.resource-unavailable`

## Fila de saída

### `os-order-events-queue.fifo`

Publicada por `SqsEventPublisher`.

### Payload (corpo JSON) publicado

Campos publicados (DTO `ServiceOrderEventDto`):

- `eventType` (String)
- `orderId` (Long)
- `customerId` (Long)
- `customerName` (String)
- `vehicleId` (Long)
- `vehicleLicensePlate` (String)
- `status` (String)
- `description` (String)
- `timestamp` (LocalDateTime)

Valores possíveis para `eventType`:

- `ORDER_CREATED`
- `ORDER_WAITING_APPROVAL`
- `ORDER_APPROVED`
- `ORDER_REJECTED`
- `ORDER_FINISHED`
- `ORDER_DELIVERED`
- `ORDER_CANCELLED`

Exemplo:

```json
{
  "eventType": "ORDER_CREATED",
  "orderId": 123,
  "customerId": 456,
  "customerName": "João Silva",
  "vehicleId": 789,
  "vehicleLicensePlate": "ABC1D23",
  "status": "RECEIVED",
  "description": "Troca de pastilha de freio",
  "timestamp": "2026-02-12T15:04:05"
}
```

### Message Attributes publicados

- `eventType` (String)
- `orderId` (String)

### Propriedades FIFO publicadas

- `messageGroupId`: valor fixo `os-service-events`
- `messageDeduplicationId`: formato `<orderId>-<eventType>-<timestampMillis>`

## Filas de entrada

Consumidas por `SqsEventListener`.

### `quote-approved-queue`

- Campo esperado no JSON: `orderId` (**obrigatório**)
- Demais campos são ignorados
- Ação executada: atualiza status para `WAITING_APPROVAL`

Exemplo mínimo:

```json
{ "orderId": 123 }
```

### `execution-completed-queue`

- Campo esperado no JSON: `orderId` (**obrigatório**)
- Demais campos são ignorados
- Ação executada: atualiza status para `FINISHED`

Exemplo mínimo:

```json
{ "orderId": 123 }
```

### `payment-failed-queue`

- Campo esperado no JSON: `orderId` (**obrigatório**)
- Campo opcional: `reason` (quando ausente, usa default `Payment failed`)
- Ação executada: cancela a OS (`cancelUseCase.execute(orderId, reason)`)

Exemplo:

```json
{ "orderId": 123, "reason": "CARD_DECLINED" }
```

### `resource-unavailable-queue`

- Campo esperado no JSON: `orderId` (**obrigatório**)
- Campo opcional: `reason` (quando ausente, usa default `Resource unavailable`)
- Ação executada: cancela a OS (`cancelUseCase.execute(orderId, reason)`)

Exemplo:

```json
{ "orderId": 123, "reason": "OUT_OF_STOCK" }
```
