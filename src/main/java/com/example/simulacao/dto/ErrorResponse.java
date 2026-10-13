package com.example.simulacao.dto;

import java.util.List;

public record ErrorResponse(List<String> message) {
}