package com.example.simulacao.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    int totalPages,
    long totalElements,
    boolean first,
    boolean last,
    boolean empty
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber() + 1,
            page.getSize(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.isFirst(),
            page.isLast(),
            page.isEmpty()
        );
    }
}