package com.example.simulacao.controller;

import com.example.simulacao.dto.ClientePatchRequest;
import com.example.simulacao.dto.ClienteRequest;
import com.example.simulacao.dto.ClienteResponse;
import com.example.simulacao.entity.Estado;
import com.example.simulacao.exception.ClienteNaoEncontradoException;
import com.example.simulacao.exception.ClientePossuiSimulacoesException;
import com.example.simulacao.exception.CpfJaCadastradoException;
import com.example.simulacao.service.ClienteService;
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

@WebMvcTest(ClienteController.class)
public class ClienteControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    @Test
    void deveListarCliente() throws Exception {
        ClienteResponse response = new ClienteResponse(
            "12345678908",
            "Felipe Ferreira",
            new ClienteResponse.EnderecoResponse(
                "Rua das Flores",
                100,
                "Centro",
                "13330000",
                "Indaiatuba",
                Estado.SP
            )
        );

        when(clienteService.findById(1L))
            .thenReturn(response);

        mockMvc.perform(get("/clientes/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cpf").value("12345678908"))
            .andExpect(content().contentType("application/json"))
        ;

        verify(clienteService).findById(1L);
    }

    @Test
    void deveRetornarNotFoundAoListarCliente() throws Exception {
        when(clienteService.findById(1L))
            .thenThrow(ClienteNaoEncontradoException.class);

        mockMvc.perform(get("/clientes/1"))
            .andExpect(status().isNotFound());

        verify(clienteService).findById(1L);
    }

    @Test
    void deveListarClientes() throws Exception {
        ClienteResponse response = new ClienteResponse(
            "12345678908",
            "Felipe Ferreira",
            new ClienteResponse.EnderecoResponse(
                "Rua das Flores",
                100,
                "Centro",
                "13330000",
                "Indaiatuba",
                Estado.SP
            )
        );

        when(clienteService.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/clientes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].cpf").value("12345678908"));

        verify(clienteService).findAll(PageRequest.of(0, 10));
    }

    @Test
    void deveCadastrarCliente() throws Exception {
        ClienteResponse response = new ClienteResponse(
            "12345678908",
            "Felipe Ferreira",
            new ClienteResponse.EnderecoResponse(
                "Rua das Flores",
                100,
                "Centro",
                "13330000",
                "Indaiatuba",
                Estado.SP
            )
        );

        ClienteRequest request = new ClienteRequest(
            "12345678908",
            "Felipe Ferreira",
            new ClienteRequest.EnderecoRequest(
                "Rua das Flores",
                100,
                "Centro",
                "13330000",
                "Indaiatuba",
                Estado.SP
            )
        );

        when(clienteService.save(any(ClienteRequest.class)))
            .thenReturn(response);

        mockMvc.perform(
                post("/clientes")
                    .content(new ObjectMapper().writeValueAsString(request))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.cpf").value("12345678908"));

        verify(clienteService, times(1)).save(request);
    }

    @Test
    void deveRetornarConflictQuandoCpfJaExisteAoCadastrarCliente() throws Exception {
        ClienteRequest request = new ClienteRequest(
            "12345678908",
            "Felipe Ferreira",
            new ClienteRequest.EnderecoRequest(
                "Rua das Flores",
                100,
                "Centro",
                "13330000",
                "Indaiatuba",
                Estado.SP
            )
        );

        when(clienteService.save(any(ClienteRequest.class)))
            .thenThrow(CpfJaCadastradoException.class);

        mockMvc.perform(
            post("/clientes")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isConflict());

        verify(clienteService, times(1)).save(request);
    }

    @Test
    void deveAtualizarClientePatch() throws Exception {
        ClienteResponse response = new ClienteResponse(
            "12345678908",
            "Felipe Teste",
            new ClienteResponse.EnderecoResponse(
                "Rua das Flores",
                100,
                "Centro",
                "13330000",
                "Indaiatuba",
                Estado.SP
            )
        );

        ClientePatchRequest request = new ClientePatchRequest(
            null,
            "Felipe Teste",
            null
        );

        when(clienteService.patch(eq(1L), any(ClientePatchRequest.class)))
            .thenReturn(response);

        mockMvc.perform(
            patch("/clientes/1")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Felipe Teste"))
        ;

        verify(clienteService, times(1)).patch(1L, request);
    }

    @Test
    void deveRetornarNotFoundAoAtualizarClientePatch() throws Exception {
        ClientePatchRequest request = new ClientePatchRequest(
            null,
            "Felipe Teste",
            null
        );

        when(clienteService.patch(eq(1L), any(ClientePatchRequest.class)))
            .thenThrow(ClienteNaoEncontradoException.class);

        mockMvc.perform(
            patch("/clientes/1")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNotFound())
        ;

        verify(clienteService, times(1)).patch(1L, request);
    }

    @Test
    void deveRetornarConflictAoAtualizarClientePatch() throws Exception {
        ClientePatchRequest request = new ClientePatchRequest(
            null,
            "Felipe Teste",
            null
        );

        when(clienteService.patch(eq(1L), any(ClientePatchRequest.class)))
            .thenThrow(CpfJaCadastradoException.class);

        mockMvc.perform(
            patch("/clientes/1")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isConflict())
        ;

        verify(clienteService, times(1)).patch(1L, request);
    }

    @Test
    void deveAtualizarClientePut() throws Exception {
        ClienteResponse response = new ClienteResponse(
            "12345678908",
            "Felipe Ferreira",
            new ClienteResponse.EnderecoResponse(
                "Rua das Flores",
                100,
                "Centro",
                "13330000",
                "Indaiatuba",
                Estado.SP
            )
        );

        ClienteRequest request = new ClienteRequest(
            "12345678908",
            "Felipe Ferreira",
            new ClienteRequest.EnderecoRequest(
                "Rua das Flores",
                100,
                "Centro",
                "13330000",
                "Indaiatuba",
                Estado.SP
            )
        );

        when(clienteService.put(eq(1L), any(ClienteRequest.class)))
            .thenReturn(response);

        mockMvc.perform(
                put("/clientes/1")
                    .content(new ObjectMapper().writeValueAsString(request))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cpf").value("12345678908"));

        verify(clienteService, times(1)).put(1L, request);
    }

    @Test
    void deveRetornarNotFoundAoAtualizarClientePut() throws Exception {
        ClienteRequest request = new ClienteRequest(
            "12345678908",
            "Felipe Ferreira",
            new ClienteRequest.EnderecoRequest(
                "Rua das Flores",
                100,
                "Centro",
                "13330000",
                "Indaiatuba",
                Estado.SP
            )
        );

        when(clienteService.put(eq(1L), any(ClienteRequest.class)))
            .thenThrow(ClienteNaoEncontradoException.class);

        mockMvc.perform(
            put("/clientes/1")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNotFound())
        ;

        verify(clienteService, times(1)).put(1L, request);
    }

    @Test
    void deveRetornarConflictAoAtualizarClientePut() throws Exception {
        ClienteRequest request = new ClienteRequest(
            "12345678908",
            "Felipe Ferreira",
            new ClienteRequest.EnderecoRequest(
                "Rua das Flores",
                100,
                "Centro",
                "13330000",
                "Indaiatuba",
                Estado.SP
            )
        );

        when(clienteService.put(eq(1L), any(ClienteRequest.class)))
            .thenThrow(CpfJaCadastradoException.class);

        mockMvc.perform(
            put("/clientes/1")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isConflict())
        ;

        verify(clienteService, times(1)).put(1L, request);
    }

    @Test
    void deveDeletarCliente() throws Exception {
        doNothing()
            .when(clienteService).delete(1L);

        mockMvc.perform(delete("/clientes/1"))
            .andExpect(status().isNoContent());

        verify(clienteService, times(1)).delete(1L);
    }

    @Test
    void deveRetornarConflictAoDeletarCliente() throws Exception {
        doThrow(ClientePossuiSimulacoesException.class)
            .when(clienteService).delete(1L);

        mockMvc.perform(delete("/clientes/1"))
            .andExpect(status().isConflict());

        verify(clienteService, times(1)).delete(1L);
    }

    @Test
    void deveRetornarNotFoundAoDeletarCliente() throws Exception {
        doThrow(ClienteNaoEncontradoException.class)
            .when(clienteService).delete(1L);

        mockMvc.perform(delete("/clientes/1"))
            .andExpect(status().isNotFound());

        verify(clienteService, times(1)).delete(1L);
    }
}
