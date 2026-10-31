package com.example.simulation.exception;

import com.example.simulation.dto.ErrorResponse;
import com.example.simulation.enums.ExportFormat;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentTypeMismatchException e) {
        if (e.getRequiredType() == ExportFormat.class) {
            String formatosPermitidos = Arrays.stream(ExportFormat.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));

            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                    "Formato inválido. Os formatos permitidos são: " + formatosPermitidos + "."
                ));
        }

        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleInvalidJson(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(Map.of(
            "message", e.getMessage()
        ));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<?> handleValidation(
        HandlerMethodValidationException ex
    ) {
        List<String> messages = ex.getParameterValidationResults()
            .stream()
            // flatMap transforma N listas em uma lista só
            .flatMap(result -> result.getResolvableErrors().stream())
            .map(MessageSourceResolvable::getDefaultMessage)
            .toList();

        return ResponseEntity
            .badRequest()
            .body(new ErrorResponse(messages.getFirst()));
    }
}
