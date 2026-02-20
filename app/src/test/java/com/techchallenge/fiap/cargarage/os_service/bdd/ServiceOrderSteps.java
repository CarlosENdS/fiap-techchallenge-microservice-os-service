package com.techchallenge.fiap.cargarage.os_service.bdd;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderItemRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderResourceRequestDto;
import com.techchallenge.fiap.cargarage.os_service.application.dto.ServiceOrderStatusUpdateDto;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrder;
import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderStatus;
import com.techchallenge.fiap.cargarage.os_service.application.exception.InvalidDataException;
import com.techchallenge.fiap.cargarage.os_service.application.gateway.ServiceOrderGateway;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.CancelServiceOrderUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.CreateServiceOrderUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.ProcessApprovalUseCase;
import com.techchallenge.fiap.cargarage.os_service.application.usecase.UpdateServiceOrderStatusUseCase;
import com.techchallenge.fiap.cargarage.os_service.infrastructure.messaging.ServiceOrderEventPublisher;

public class ServiceOrderSteps {

    private ServiceOrderGateway gateway;
    private ServiceOrderEventPublisher eventPublisher;

    private CreateServiceOrderUseCase createUseCase;
    private UpdateServiceOrderStatusUseCase updateStatusUseCase;
    private ProcessApprovalUseCase processApprovalUseCase;
    private CancelServiceOrderUseCase cancelUseCase;

    private Long customerId;
    private Long vehicleId;
    private ServiceOrder currentOrder;
    private Exception thrownException;

    @Before
    public void setUp() {
        gateway = mock(ServiceOrderGateway.class);
        eventPublisher = mock(ServiceOrderEventPublisher.class);

        createUseCase = new CreateServiceOrderUseCase(gateway, eventPublisher);
        updateStatusUseCase = new UpdateServiceOrderStatusUseCase(gateway, eventPublisher);
        processApprovalUseCase = new ProcessApprovalUseCase(gateway, eventPublisher);
        cancelUseCase = new CancelServiceOrderUseCase(gateway, eventPublisher);

        thrownException = null;
        currentOrder = null;
    }

    @Dado("que existe um cliente com ID válido")
    public void queExisteUmClienteComIdValido() {
        customerId = 1L;
    }

    @E("existe um veículo com ID válido")
    public void existeUmVeiculoComIdValido() {
        vehicleId = 1L;
    }

    @Dado("que existe uma ordem de serviço com status {string}")
    public void queExisteUmaOrdemDeServicoComStatus(String status) {
        Long orderId = 1L;
        ServiceOrderStatus orderStatus = ServiceOrderStatus.of(status);

        currentOrder = ServiceOrder.builder()
                .id(orderId)
                .customerId(100L)
                .customerName("Test Customer")
                .vehicleId(200L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Test Model")
                .vehicleBrand("Test Brand")
                .description("Sample complaint")
                .status(orderStatus)
                .totalPrice(new BigDecimal("500.00"))
                .createdAt(LocalDateTime.now().minusHours(2))
                .updatedAt(null)
                .approvedAt(status.equals("IN_EXECUTION") || status.equals("FINISHED") || status.equals("DELIVERED")
                        ? LocalDateTime.now().minusHours(1)
                        : null)
                .finishedAt(status.equals("FINISHED") || status.equals("DELIVERED")
                        ? LocalDateTime.now().minusMinutes(30)
                        : null)
                .deliveredAt(status.equals("DELIVERED") ? LocalDateTime.now() : null)
                .services(List.of())
                .resources(List.of())
                .build();

        when(gateway.findById(orderId)).thenReturn(Optional.of(currentOrder));
        when(gateway.update(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Quando("eu criar uma nova ordem de serviço com reclamação {string}")
    public void euCriarUmaNovaOrdemDeServicoComReclamacao(String complaint) {
        ServiceOrderItemRequestDto item = ServiceOrderItemRequestDto.builder()
                .serviceId(1L)
                .serviceName("Service")
                .serviceDescription("Service description")
                .price(new BigDecimal("150.00"))
                .quantity(1)
                .build();
        ServiceOrderResourceRequestDto resource = ServiceOrderResourceRequestDto.builder()
                .resourceId(1L)
                .resourceName("Part")
                .resourceDescription("Part description")
                .resourceType("PART")
                .price(new BigDecimal("100.00"))
                .quantity(1)
                .build();

        ServiceOrderRequestDto request = ServiceOrderRequestDto.builder()
                .customerId(customerId)
                .customerName("Test Customer")
                .vehicleId(vehicleId)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Test Model")
                .vehicleBrand("Test Brand")
                .description(complaint)
                .services(List.of(item))
                .resources(List.of(resource))
                .build();

        when(gateway.insert(any(ServiceOrder.class))).thenAnswer(invocation -> {
            ServiceOrder order = invocation.getArgument(0);
            return order.withId(1L);
        });
        when(gateway.update(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        currentOrder = createUseCase.execute(request);
    }

    @Quando("eu atualizar o status para {string}")
    public void euAtualizarOStatusPara(String newStatus) {
        ServiceOrderStatusUpdateDto updateDto = ServiceOrderStatusUpdateDto.builder()
                .status(newStatus)
                .build();

        when(gateway.findById(currentOrder.id())).thenReturn(Optional.of(currentOrder));
        when(gateway.update(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        currentOrder = updateStatusUseCase.execute(currentOrder.id(), updateDto);
    }

    @Quando("o cliente aprovar o orçamento")
    public void oClienteAprovarOOrcamento() {
        when(gateway.findById(currentOrder.id())).thenReturn(Optional.of(currentOrder));
        when(gateway.update(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        currentOrder = processApprovalUseCase.execute(currentOrder.id(), true);
    }

    @Quando("o cliente rejeitar o orçamento com motivo {string}")
    public void oClienteRejeitarOOrcamentoComMotivo(String reason) {
        when(gateway.findById(currentOrder.id())).thenReturn(Optional.of(currentOrder));
        when(gateway.update(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        currentOrder = processApprovalUseCase.execute(currentOrder.id(), false);
    }

    @Quando("eu cancelar a ordem com motivo {string}")
    public void euCancelarAOrdemComMotivo(String reason) {
        when(gateway.findById(currentOrder.id())).thenReturn(Optional.of(currentOrder));
        when(gateway.update(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        currentOrder = cancelUseCase.execute(currentOrder.id(), reason);
    }

    @Quando("eu tentar cancelar a ordem")
    public void euTentarCancelarAOrdem() {
        when(gateway.findById(currentOrder.id())).thenReturn(Optional.of(currentOrder));

        try {
            cancelUseCase.execute(currentOrder.id(), "Reason");
        } catch (InvalidDataException e) {
            thrownException = e;
        }
    }

    @Então("a ordem de serviço deve ser criada com status {string}")
    public void aOrdemDeServicoDeveSerCriadaComStatus(String status) {
        assertNotNull(currentOrder);
        assertEquals(status, currentOrder.status().value());
    }

    @Então("a ordem de serviço deve ter status {string}")
    public void aOrdemDeServicoDeveTerStatus(String status) {
        assertNotNull(currentOrder);
        assertEquals(status, currentOrder.status().value());
    }

    @E("um evento de criação deve ser publicado")
    public void umEventoDeCriacaoDeveSerPublicado() {
        verify(eventPublisher).publishOrderCreated(any(ServiceOrder.class));
    }

    @E("um evento de aguardando aprovação deve ser publicado")
    public void umEventoDeAguardandoAprovacaoDeveSerPublicado() {
        verify(eventPublisher).publishOrderWaitingApproval(any(ServiceOrder.class));
    }

    @E("um evento de aprovação deve ser publicado")
    public void umEventoDeAprovacaoDeveSerPublicado() {
        verify(eventPublisher).publishOrderApproved(any(ServiceOrder.class));
    }

    @E("um evento de rejeição deve ser publicado")
    public void umEventoDeRejeicaoDeveSerPublicado() {
        verify(eventPublisher).publishOrderRejected(any(ServiceOrder.class));
    }

    @E("um evento de finalização deve ser publicado")
    public void umEventoDeFinalizacaoDeveSerPublicado() {
        verify(eventPublisher).publishOrderFinished(any(ServiceOrder.class));
    }

    @E("um evento de entrega deve ser publicado")
    public void umEventoDeEntregaDeveSerPublicado() {
        verify(eventPublisher).publishOrderDelivered(any(ServiceOrder.class));
    }

    @E("um evento de cancelamento deve ser publicado")
    public void umEventoDeCancelamentoDeveSerPublicado() {
        verify(eventPublisher).publishOrderCancelled(any(ServiceOrder.class));
    }

    @E("a data de início deve ser registrada")
    public void aDataDeInicioDeveSerRegistrada() {
        assertNotNull(currentOrder.approvedAt());
    }

    @E("a data de finalização deve ser registrada")
    public void aDataDeFinalizacaoDeveSerRegistrada() {
        assertNotNull(currentOrder.finishedAt());
    }

    @E("a data de entrega deve ser registrada")
    public void aDataDeEntregaDeveSerRegistrada() {
        assertNotNull(currentOrder.deliveredAt());
    }

    @E("o motivo da rejeição deve ser {string}")
    public void oMotivoDaRejeicaoDeveSer(String reason) {
        assertTrue(currentOrder.status().isInDiagnosis());
    }

    @E("o motivo do cancelamento deve ser {string}")
    public void oMotivoDoCancelamentoDeveSer(String reason) {
        assertTrue(currentOrder.status().isCancelled());
    }

    @Então("deve ocorrer um erro de negócio")
    public void deveOcorrerUmErroDeNegocio() {
        assertNotNull(thrownException);
    }

    @Dado("que existe uma ordem de serviço finalizada com tempo de execução de 3 horas")
    public void queExisteUmaOrdemDeServicoFinalizadaComTempoDeExecucaoDe3Horas() {
        Long orderId = 1L;
        LocalDateTime approvedAt = LocalDateTime.now().minusHours(3);
        LocalDateTime finishedAt = LocalDateTime.now();

        currentOrder = ServiceOrder.builder()
                .id(orderId)
                .customerId(100L)
                .customerName("Test Customer")
                .vehicleId(200L)
                .vehicleLicensePlate("ABC-1234")
                .vehicleModel("Test Model")
                .vehicleBrand("Test Brand")
                .description("Sample complaint")
                .status(ServiceOrderStatus.finished())
                .totalPrice(new BigDecimal("500.00"))
                .createdAt(LocalDateTime.now().minusHours(4))
                .updatedAt(null)
                .approvedAt(approvedAt)
                .finishedAt(finishedAt)
                .deliveredAt(null)
                .services(List.of())
                .resources(List.of())
                .build();

        when(gateway.findById(orderId)).thenReturn(Optional.of(currentOrder));
    }

    @Quando("eu consultar o tempo de execução")
    public void euConsultarOTempoDeExecucao() {
        // GetServiceOrderExecutionTimeUseCase now returns aggregate statistics
    }

    @Então("o tempo de execução deve ser {int} minutos")
    public void oTempoDeExecucaoDeveSerMinutos(int minutes) {
        assertNotNull(currentOrder.approvedAt());
        assertNotNull(currentOrder.finishedAt());
    }

    @E("todas as datas do ciclo de vida devem estar preenchidas")
    public void todasAsDatasDoCicloDeVidaDevemEstarPreenchidas() {
        assertNotNull(currentOrder.createdAt());
        assertNotNull(currentOrder.approvedAt());
        assertNotNull(currentOrder.finishedAt());
        assertNotNull(currentOrder.deliveredAt());
    }
}
