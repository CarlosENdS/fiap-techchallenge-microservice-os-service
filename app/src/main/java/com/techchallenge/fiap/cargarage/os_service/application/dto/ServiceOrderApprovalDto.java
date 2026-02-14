package com.techchallenge.fiap.cargarage.os_service.application.dto;

import lombok.Builder;

/**
 * DTO for Service Order approval request.
 */
@Builder
public record ServiceOrderApprovalDto(
        boolean approved) {
}
