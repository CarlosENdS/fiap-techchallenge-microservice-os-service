package com.techchallenge.fiap.cargarage.os_service.application.dto;

import java.util.List;

/**
 * Generic DTO for paginated responses.
 */
public record PageDto<T>(
        List<T> content,
        long totalElements,
        int pageNumber,
        int pageSize) {
}
