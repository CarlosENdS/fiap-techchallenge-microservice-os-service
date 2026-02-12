package com.techchallenge.fiap.cargarage.os_service.application.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.techchallenge.fiap.cargarage.os_service.application.entity.ServiceOrderStatus;

class ServiceOrderStatusTest {

    @Test
    @DisplayName("Should allow transition from RECEIVED to IN_DIAGNOSIS")
    void shouldAllowTransitionFromReceivedToInDiagnosis() {
        ServiceOrderStatus status = ServiceOrderStatus.received();
        assertTrue(status.canTransitionTo(ServiceOrderStatus.inDiagnosis()));
    }

    @Test
    @DisplayName("Should allow transition from IN_DIAGNOSIS to WAITING_APPROVAL")
    void shouldAllowTransitionFromInDiagnosisToWaitingApproval() {
        ServiceOrderStatus status = ServiceOrderStatus.inDiagnosis();
        assertTrue(status.canTransitionTo(ServiceOrderStatus.waitingApproval()));
    }

    @Test
    @DisplayName("Should allow transition from WAITING_APPROVAL to IN_EXECUTION")
    void shouldAllowTransitionFromWaitingApprovalToInExecution() {
        ServiceOrderStatus status = ServiceOrderStatus.waitingApproval();
        assertTrue(status.canTransitionTo(ServiceOrderStatus.inExecution()));
    }

    @Test
    @DisplayName("Should allow transition from IN_EXECUTION to FINISHED")
    void shouldAllowTransitionFromInExecutionToFinished() {
        ServiceOrderStatus status = ServiceOrderStatus.inExecution();
        assertTrue(status.canTransitionTo(ServiceOrderStatus.finished()));
    }

    @Test
    @DisplayName("Should allow transition from FINISHED to DELIVERED")
    void shouldAllowTransitionFromFinishedToDelivered() {
        ServiceOrderStatus status = ServiceOrderStatus.finished();
        assertTrue(status.canTransitionTo(ServiceOrderStatus.delivered()));
    }

    @Test
    @DisplayName("Should allow transition to CANCELLED from RECEIVED")
    void shouldAllowTransitionToCancelledFromReceived() {
        ServiceOrderStatus status = ServiceOrderStatus.received();
        assertTrue(status.canTransitionTo(ServiceOrderStatus.cancelled()));
    }

    @Test
    @DisplayName("Should allow transition to CANCELLED from IN_DIAGNOSIS")
    void shouldAllowTransitionToCancelledFromInDiagnosis() {
        ServiceOrderStatus status = ServiceOrderStatus.inDiagnosis();
        assertTrue(status.canTransitionTo(ServiceOrderStatus.cancelled()));
    }

    @Test
    @DisplayName("Should allow transition to CANCELLED from WAITING_APPROVAL")
    void shouldAllowTransitionToCancelledFromWaitingApproval() {
        ServiceOrderStatus status = ServiceOrderStatus.waitingApproval();
        assertTrue(status.canTransitionTo(ServiceOrderStatus.cancelled()));
    }

    @Test
    @DisplayName("Should not allow transition from IN_EXECUTION to CANCELLED")
    void shouldNotAllowTransitionFromInExecutionToCancelled() {
        ServiceOrderStatus status = ServiceOrderStatus.inExecution();
        assertFalse(status.canTransitionTo(ServiceOrderStatus.cancelled()));
    }

    @Test
    @DisplayName("Should not allow transition from FINISHED to CANCELLED")
    void shouldNotAllowTransitionFromFinishedToCancelled() {
        ServiceOrderStatus status = ServiceOrderStatus.finished();
        assertFalse(status.canTransitionTo(ServiceOrderStatus.cancelled()));
    }

    @Test
    @DisplayName("Should not allow transition from DELIVERED to any state")
    void shouldNotAllowTransitionFromDeliveredToAnyState() {
        ServiceOrderStatus status = ServiceOrderStatus.delivered();

        assertFalse(status.canTransitionTo(ServiceOrderStatus.received()));
        assertFalse(status.canTransitionTo(ServiceOrderStatus.inDiagnosis()));
        assertFalse(status.canTransitionTo(ServiceOrderStatus.waitingApproval()));
        assertFalse(status.canTransitionTo(ServiceOrderStatus.inExecution()));
        assertFalse(status.canTransitionTo(ServiceOrderStatus.finished()));
        assertFalse(status.canTransitionTo(ServiceOrderStatus.cancelled()));
    }

    @Test
    @DisplayName("Should not allow transition from CANCELLED to any state")
    void shouldNotAllowTransitionFromCancelledToAnyState() {
        ServiceOrderStatus status = ServiceOrderStatus.cancelled();

        assertFalse(status.canTransitionTo(ServiceOrderStatus.received()));
        assertFalse(status.canTransitionTo(ServiceOrderStatus.inDiagnosis()));
        assertFalse(status.canTransitionTo(ServiceOrderStatus.waitingApproval()));
        assertFalse(status.canTransitionTo(ServiceOrderStatus.inExecution()));
        assertFalse(status.canTransitionTo(ServiceOrderStatus.finished()));
        assertFalse(status.canTransitionTo(ServiceOrderStatus.delivered()));
    }

    @Test
    @DisplayName("Should not allow skipping states")
    void shouldNotAllowSkippingStates() {
        ServiceOrderStatus status = ServiceOrderStatus.received();

        assertFalse(status.canTransitionTo(ServiceOrderStatus.waitingApproval()));
        assertFalse(status.canTransitionTo(ServiceOrderStatus.inExecution()));
        assertFalse(status.canTransitionTo(ServiceOrderStatus.finished()));
        assertFalse(status.canTransitionTo(ServiceOrderStatus.delivered()));
    }

    @Test
    @DisplayName("Should not allow backward transitions")
    void shouldNotAllowBackwardTransitions() {
        ServiceOrderStatus status = ServiceOrderStatus.inExecution();

        assertFalse(status.canTransitionTo(ServiceOrderStatus.received()));
        assertFalse(status.canTransitionTo(ServiceOrderStatus.inDiagnosis()));
        assertFalse(status.canTransitionTo(ServiceOrderStatus.waitingApproval()));
    }

    @Test
    @DisplayName("Should not allow transition to same state")
    void shouldNotAllowTransitionToSameState() {
        ServiceOrderStatus status = ServiceOrderStatus.inExecution();

        assertFalse(status.canTransitionTo(ServiceOrderStatus.inExecution()));
    }

    @Test
    @DisplayName("Should return correct value for each status")
    void shouldReturnCorrectValueForEachStatus() {
        assertEquals("RECEIVED", ServiceOrderStatus.received().value());
        assertEquals("IN_DIAGNOSIS", ServiceOrderStatus.inDiagnosis().value());
        assertEquals("WAITING_APPROVAL", ServiceOrderStatus.waitingApproval().value());
        assertEquals("IN_EXECUTION", ServiceOrderStatus.inExecution().value());
        assertEquals("FINISHED", ServiceOrderStatus.finished().value());
        assertEquals("DELIVERED", ServiceOrderStatus.delivered().value());
        assertEquals("CANCELLED", ServiceOrderStatus.cancelled().value());
    }

    @Test
    @DisplayName("Should create status from string using of() factory")
    void shouldCreateStatusFromStringUsingOfFactory() {
        ServiceOrderStatus status = ServiceOrderStatus.of("RECEIVED");
        assertEquals("RECEIVED", status.value());
        assertTrue(status.isReceived());
    }
}
