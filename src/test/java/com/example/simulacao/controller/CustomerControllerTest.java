package com.example.simulacao.controller;

import com.example.simulacao.dto.CustomerPatchRequest;
import com.example.simulacao.dto.CustomerRequest;
import com.example.simulacao.dto.CustomerResponse;
import com.example.simulacao.entity.Estado;
import com.example.simulacao.exception.CustomerNaoEncontradoException;
import com.example.simulacao.exception.CustomerPossuiSimulacoesException;
import com.example.simulacao.exception.CpfJaCadastradoException;
import com.example.simulacao.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
public class CustomerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Test
    void deveListarCustomer() throws Exception {
        CustomerResponse response = new CustomerResponse(
            "12345678908",
            "Felipe Ferreira",
            new CustomerResponse.AddressResponse(
                "Rua das Flores",
                100,
                "Centro",
                "13330000",
                "Indaiatuba",
                Estado.SP
            )
        );

        when(customerService.findById(1L))
            .thenReturn(response);

        mockMvc.perform(get("/customers/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cpf").value("12345678908"))
            .andExpect(jsonPath("$.name").value("Felipe Ferreira"))
            .andExpect(content().contentType("application/json"));

        verify(customerService).findById(1L);
    }

    @Test
    void deveRetornarNotFoundAoListarCustomer() throws Exception {
        when(customerService.findById(1L))
            .thenThrow(CustomerNaoEncontradoException.class);

        mockMvc.perform(get("/customers/1"))
            .andExpect(status().isNotFound());

        verify(customerService).findById(1L);
    }

    @Test
    void deveListarCustomers() throws Exception {
        CustomerResponse response = new CustomerResponse(
            "12345678908",
            "Felipe Ferreira",
            new CustomerResponse.AddressResponse(
                "Rua das Flores",
                100,
                "Centro",
                "13330000",
                "Indaiatuba",
                Estado.SP
            )
        );

        when(customerService.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/customers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].cpf").value("12345678908"))
            .andExpect(jsonPath("$.content[0].name").value("Felipe Ferreira"));

        verify(customerService).findAll(PageRequest.of(0, 10));
    }

    @Test
    void deveCadastrarCustomer() throws Exception {
        CustomerResponse response = new CustomerResponse(
            "12345678908",
            "Felipe Ferreira",
            new CustomerResponse.AddressResponse(
                "Rua das Flores",
                100,
                "Centro",
                "13330000",
                "Indaiatuba",
                Estado.SP
            )
        );

        CustomerRequest request = new CustomerRequest(
            "12345678908",
            "Felipe Ferreira",
            new CustomerRequest.AddressRequest(
                "Rua das Flores",
                100,
                "Centro",
                "13330000",
                "Indaiatuba",
                Estado.SP
            )
        );

        when(customerService.save(any(CustomerRequest.class)))
            .thenReturn(response);

        mockMvc.perform(
            post("/customers")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.cpf").value("12345678908"))
        .andExpect(jsonPath("$.name").value("Felipe Ferreira"));

        verify(customerService, times(1)).save(request);
    }

    @Test
    void deveRetornarConflictQuandoCpfJaExisteAoCadastrarCustomer() throws Exception {
        CustomerRequest request = new CustomerRequest(
            "12345678908",
            "Felipe Ferreira",
            new CustomerRequest.AddressRequest(
                "Rua das Flores",
                100,
                "Centro",
                "13330000",
                "Indaiatuba",
                Estado.SP
            )
        );

        when(customerService.save(any(CustomerRequest.class)))
            .thenThrow(CpfJaCadastradoException.class);

        mockMvc.perform(
            post("/customers")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isConflict());

        verify(customerService, times(1)).save(request);
    }

    @Test
    void deveAtualizarCustomerPatch() throws Exception {
        CustomerResponse response = new CustomerResponse(
            "12345678908",
            "Felipe Teste",
            new CustomerResponse.AddressResponse(
                "Rua das Flores",
                100,
                "Centro",
                "13330000",
                "Indaiatuba",
                Estado.SP
            )
        );

        CustomerPatchRequest request = new CustomerPatchRequest(
            null,
            "Felipe Teste",
            null
        );

        when(customerService.patch(eq(1L), any(CustomerPatchRequest.class)))
            .thenReturn(response);

        mockMvc.perform(
            patch("/customers/1")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Felipe Teste"));

        verify(customerService, times(1)).patch(1L, request);
    }

    @Test
    void deveRetornarNotFoundAoAtualizarCustomerPatch() throws Exception {
        CustomerPatchRequest request = new CustomerPatchRequest(
            null,
            "Felipe Teste",
            null
        );

        when(customerService.patch(eq(1L), any(CustomerPatchRequest.class)))
            .thenThrow(CustomerNaoEncontradoException.class);

        mockMvc.perform(
            patch("/customers/1")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound());

        verify(customerService, times(1)).patch(1L, request);
    }

    @Test
    void deveRetornarConflictAoAtualizarCustomerPatch() throws Exception {
        CustomerPatchRequest request = new CustomerPatchRequest(
            null,
            "Felipe Teste",
            null
        );

        when(customerService.patch(eq(1L), any(CustomerPatchRequest.class)))
            .thenThrow(CpfJaCadastradoException.class);

        mockMvc.perform(
            patch("/customers/1")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isConflict());

        verify(customerService, times(1)).patch(1L, request);
    }

    @Test
    void deveAtualizarCustomerPut() throws Exception {
        CustomerResponse response = new CustomerResponse(
            "12345678908",
            "Felipe Ferreira",
            new CustomerResponse.AddressResponse(
                "Rua das Flores",
                100,
                "Centro",
                "13330000",
                "Indaiatuba",
                Estado.SP
            )
        );

        CustomerRequest request = new CustomerRequest(
            "12345678908",
            "Felipe Ferreira",
            new CustomerRequest.AddressRequest(
                "Rua das Flores",
                100,
                "Centro",
                "13330000",
                "Indaiatuba",
                Estado.SP
            )
        );

        when(customerService.put(eq(1L), any(CustomerRequest.class)))
            .thenReturn(response);

        mockMvc.perform(
            put("/customers/1")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cpf").value("12345678908"));

        verify(customerService, times(1)).put(1L, request);
    }

    @Test
    void deveRetornarNotFoundAoAtualizarCustomerPut() throws Exception {
        CustomerRequest request = new CustomerRequest(
            "12345678908",
            "Felipe Ferreira",
            new CustomerRequest.AddressRequest(
                "Rua das Flores",
                100,
                "Centro",
                "13330000",
                "Indaiatuba",
                Estado.SP
            )
        );

        when(customerService.put(eq(1L), any(CustomerRequest.class)))
            .thenThrow(CustomerNaoEncontradoException.class);

        mockMvc.perform(
            put("/customers/1")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound());

        verify(customerService, times(1)).put(1L, request);
    }

    @Test
    void deveRetornarConflictAoAtualizarCustomerPut() throws Exception {
        CustomerRequest request = new CustomerRequest(
            "12345678908",
            "Felipe Ferreira",
            new CustomerRequest.AddressRequest(
                "Rua das Flores",
                100,
                "Centro",
                "13330000",
                "Indaiatuba",
                Estado.SP
            )
        );

        when(customerService.put(eq(1L), any(CustomerRequest.class)))
            .thenThrow(CpfJaCadastradoException.class);

        mockMvc.perform(
            put("/customers/1")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isConflict());

        verify(customerService, times(1)).put(1L, request);
    }

    @Test
    void deveDeletarCustomer() throws Exception {
        doNothing()
            .when(customerService).delete(1L);

        mockMvc.perform(delete("/customers/1"))
            .andExpect(status().isNoContent());

        verify(customerService, times(1)).delete(1L);
    }

    @Test
    void deveRetornarConflictAoDeletarCustomer() throws Exception {
        doThrow(CustomerPossuiSimulacoesException.class)
            .when(customerService).delete(1L);

        mockMvc.perform(delete("/customers/1"))
            .andExpect(status().isConflict());

        verify(customerService, times(1)).delete(1L);
    }

    @Test
    void deveRetornarNotFoundAoDeletarCustomer() throws Exception {
        doThrow(CustomerNaoEncontradoException.class)
            .when(customerService).delete(1L);

        mockMvc.perform(delete("/customers/1"))
            .andExpect(status().isNotFound());

        verify(customerService, times(1)).delete(1L);
    }
}
