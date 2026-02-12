# language: pt
Funcionalidade: Gerenciamento de Ordens de Serviço (OS)
  Como um usuário do sistema Car Garage
  Eu quero gerenciar ordens de serviço
  Para controlar o fluxo de trabalho de manutenção de veículos

  Cenário: Criar nova ordem de serviço
    Dado que existe um cliente com ID válido
    E existe um veículo com ID válido
    Quando eu criar uma nova ordem de serviço com reclamação "Barulho no motor"
    Então a ordem de serviço deve ser criada com status "RECEIVED"
    E um evento de criação deve ser publicado

  Cenário: Atualizar status da ordem de serviço para diagnóstico
    Dado que existe uma ordem de serviço com status "RECEIVED"
    Quando eu atualizar o status para "IN_DIAGNOSIS"
    Então a ordem de serviço deve ter status "IN_DIAGNOSIS"

  Cenário: Enviar ordem para aprovação após diagnóstico
    Dado que existe uma ordem de serviço com status "IN_DIAGNOSIS"
    Quando eu atualizar o status para "WAITING_APPROVAL"
    Então a ordem de serviço deve ter status "WAITING_APPROVAL"
    E um evento de aguardando aprovação deve ser publicado

  Cenário: Aprovar orçamento e iniciar execução
    Dado que existe uma ordem de serviço com status "WAITING_APPROVAL"
    Quando o cliente aprovar o orçamento
    Então a ordem de serviço deve ter status "IN_EXECUTION"
    E a data de início deve ser registrada
    E um evento de aprovação deve ser publicado

  Cenário: Rejeitar orçamento e retornar para diagnóstico
    Dado que existe uma ordem de serviço com status "WAITING_APPROVAL"
    Quando o cliente rejeitar o orçamento com motivo "Preço muito alto"
    Então a ordem de serviço deve ter status "IN_DIAGNOSIS"
    E um evento de rejeição deve ser publicado

  Cenário: Finalizar serviço após execução
    Dado que existe uma ordem de serviço com status "IN_EXECUTION"
    Quando eu atualizar o status para "FINISHED"
    Então a ordem de serviço deve ter status "FINISHED"
    E a data de finalização deve ser registrada
    E um evento de finalização deve ser publicado

  Cenário: Entregar veículo ao cliente
    Dado que existe uma ordem de serviço com status "FINISHED"
    Quando eu atualizar o status para "DELIVERED"
    Então a ordem de serviço deve ter status "DELIVERED"
    E a data de entrega deve ser registrada
    E um evento de entrega deve ser publicado

  Cenário: Cancelar ordem de serviço em diagnóstico
    Dado que existe uma ordem de serviço com status "IN_DIAGNOSIS"
    Quando eu cancelar a ordem com motivo "Cliente desistiu"
    Então a ordem de serviço deve ter status "CANCELLED"
    E um evento de cancelamento deve ser publicado

  Cenário: Não permitir cancelar ordem em execução
    Dado que existe uma ordem de serviço com status "IN_EXECUTION"
    Quando eu tentar cancelar a ordem
    Então deve ocorrer um erro de negócio

  Cenário: Não permitir cancelar ordem finalizada
    Dado que existe uma ordem de serviço com status "FINISHED"
    Quando eu tentar cancelar a ordem
    Então deve ocorrer um erro de negócio

  Cenário: Calcular tempo de execução
    Dado que existe uma ordem de serviço finalizada com tempo de execução de 3 horas
    Quando eu consultar o tempo de execução
    Então o tempo de execução deve ser 180 minutos

  Cenário: Fluxo completo de ordem de serviço
    Dado que existe um cliente com ID válido
    E existe um veículo com ID válido
    Quando eu criar uma nova ordem de serviço com reclamação "Problema nos freios"
    E eu atualizar o status para "IN_DIAGNOSIS"
    E eu atualizar o status para "WAITING_APPROVAL"
    E o cliente aprovar o orçamento
    E eu atualizar o status para "FINISHED"
    E eu atualizar o status para "DELIVERED"
    Então a ordem de serviço deve ter status "DELIVERED"
    E todas as datas do ciclo de vida devem estar preenchidas
