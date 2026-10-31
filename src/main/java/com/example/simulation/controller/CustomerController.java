package com.example.simulation.controller;

import com.example.simulation.dto.*;
import com.example.simulation.exception.CustomerNaoEncontradoException;
import com.example.simulation.exception.CustomerHaveSimulationsException;
import com.example.simulation.exception.CpfAlreadyAssociatedException;
import com.example.simulation.service.CustomerService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody CustomerRequest request){
        log.info("Novo customer: {}", request.toString());

        try {
            CustomerResponse response = customerService.save(request);

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
        } catch (CpfAlreadyAssociatedException e) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Erro ao criar novo customer: {}", request, e);

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(customerService.findById(id));
        } catch (CustomerNaoEncontradoException e) {
            log.error(e.getMessage());

            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .build();
        } catch (Exception e) {
            log.error("Erro ao obter o customer: {}", id, e);

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }
    }

    @GetMapping()
    public ResponseEntity<?> findAll(
        @RequestParam(defaultValue = "1")
        @Min(value = 1, message = "A página deve ser maior ou igual a 1")
        int pagina,

        @RequestParam(name = "quantidade_por_pagina", defaultValue = "10")
        @Min(value = 1, message = "A quantidade por página deve ser maior ou igual a 1")
        @Max(value = 100, message = "A quantidade máxima por página é 100")
        int quantidadePorPagina
    ) {
        try {
            Page<CustomerResponse> list = customerService.findAll(
                // O índice da paginação inicia em zero
                PageRequest.of(pagina - 1, quantidadePorPagina)
            );

            return ResponseEntity.ok(PageResponse.from(list));
        } catch (Exception e) {
            log.error("Erro ao obter lista de customers: ", e);

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patch(
        @PathVariable Long id,
        @Valid @RequestBody CustomerPatchRequest request
    ) {
        try {
            CustomerResponse response = customerService.patch(id, request);
            return ResponseEntity.ok().body(response);
        } catch (CustomerNaoEncontradoException e) {
            log.error(e.getMessage());

            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .build();
        } catch (CpfAlreadyAssociatedException e) {
            log.error(e.getMessage());

            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Erro ao criar novo customer: {}", request, e);

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> put(
        @PathVariable Long id,
        @Valid @RequestBody CustomerRequest request
    ) {
        try {
            CustomerResponse response = customerService.put(id, request);
            return ResponseEntity.ok().body(response);
        } catch (CustomerNaoEncontradoException e) {
            log.error(e.getMessage());

            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .build();
        } catch (CpfAlreadyAssociatedException e) {
            log.error(e.getMessage());

            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Erro ao criar novo customer: {}", request, e);

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            customerService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (CustomerNaoEncontradoException e) {
            log.error(e.getMessage());

            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .build();
        } catch (CustomerHaveSimulationsException e) {
            log.error(e.getMessage());

            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Erro ao remover customer: {}", id, e);

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        }
    }
}
