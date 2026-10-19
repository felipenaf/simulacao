package com.example.simulacao.service;

import com.example.simulacao.dto.ClienteRequest;
import com.example.simulacao.dto.ClienteResponse;
import com.example.simulacao.entity.Cliente;
import com.example.simulacao.entity.Endereco;
import com.example.simulacao.entity.Estado;
import com.example.simulacao.exception.ClienteNaoEncontradoException;
import com.example.simulacao.exception.CpfJaCadastradoException;
import com.example.simulacao.repository.ClienteRepository;
import com.example.simulacao.repository.SimulacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {
    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private SimulacaoRepository simulacaoRepository;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    @Captor
    private ArgumentCaptor<Cliente> clienteCaptor;

    private ClienteRequest criarRequest() {
        ClienteRequest.EnderecoRequest endereco = new ClienteRequest.EnderecoRequest(
            "Rua das Flores",
            123,
            "Centro",
            "13000-000",
            "Campinas",
            Estado.SP
        );

        return new ClienteRequest("12345678900", "Felipe Ferreira", endereco);
    }

    private Cliente criarEntity() {
        Endereco endereco = Endereco.builder()
            .rua("Rua das Flores")
            .numero(123)
            .bairro("Centro")
            .cep("13000-000")
            .cidade("Campinas")
            .estado(Estado.SP)
            .build();

        return Cliente.builder()
            .cpf("12345678900")
            .nome("Felipe Ferreira")
            .endereco(endereco)
            .build();
    }

    @Test
    void deveSalvarCliente() {
        ClienteRequest request = criarRequest();

        when(clienteRepository.findByCpf(request.cpf()))
            .thenReturn(Optional.empty());

        ClienteResponse response = clienteService.save(request);

        assertNotNull(response);

        // Aqui está sendo interceptado o objeto Cliente que é criado dentro
        // da função save do ClienteService e enviado ao repository e não tenho acesso.
        verify(clienteRepository).save(clienteCaptor.capture());

        // Garante que apenas uma chamada foi feita
        assertEquals(1, clienteCaptor.getAllValues().size());

        Cliente salvo = clienteCaptor.getValue();

        assertEquals(request.cpf(), salvo.getCpf());
        assertEquals(request.nome(), salvo.getNome());

        assertEquals(request.endereco().rua(), salvo.getEndereco().getRua());
        assertEquals(request.endereco().numero(), salvo.getEndereco().getNumero());
        assertEquals(request.endereco().bairro(), salvo.getEndereco().getBairro());
        assertEquals(request.endereco().cep(), salvo.getEndereco().getCep());
        assertEquals(request.endereco().cidade(), salvo.getEndereco().getCidade());
        assertEquals(request.endereco().estado(), salvo.getEndereco().getEstado());
    }

    @Test
    void deveRetornarResponseCorretoAoSalvarCliente() {
        ClienteRequest request = criarRequest();
        when(clienteRepository.findByCpf(request.cpf())).thenReturn(Optional.empty());

        ClienteResponse response = clienteService.save(request);

        assertEquals(request.cpf(), response.cpf());
        assertEquals(request.nome(), response.nome());
        assertEquals(request.endereco().rua(), response.endereco().rua());
        assertEquals(request.endereco().numero(), response.endereco().numero());
        assertEquals(request.endereco().bairro(), response.endereco().bairro());
        assertEquals(request.endereco().cep(), response.endereco().cep());
        assertEquals(request.endereco().cidade(), response.endereco().cidade());
        assertEquals(request.endereco().estado(), response.endereco().estado());
    }

    @Test
    void deveLancarCpfJaCadastradoExceptionAoSalvarCliente() {
        ClienteRequest request = criarRequest();

        when(clienteRepository.findByCpf(request.cpf()))
            .thenReturn(Optional.of(Cliente.builder().cpf(request.cpf()).build()));

        CpfJaCadastradoException ex = assertThrows(
            CpfJaCadastradoException.class,
            () -> clienteService.save(request)
        );

        assertEquals("CPF já cadastrado", ex.getMessage());

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoSalvarClienteComRequestNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> clienteService.save(null)
        );
    }

    @Test
    void deveConsultarCliente() {
        Cliente cliente = criarEntity();

        when(clienteRepository.findById(1L))
            .thenReturn(Optional.of(cliente));

        ClienteResponse response = clienteService.findById(1L);

        verify(clienteRepository).findById(1L);

        assertNotNull(response);
        assertEquals(response.cpf(), cliente.getCpf());
        assertEquals(response.nome(), cliente.getNome());
        assertEquals(response.endereco().rua(), cliente.getEndereco().getRua());
        assertEquals(response.endereco().numero(), cliente.getEndereco().getNumero());
        assertEquals(response.endereco().bairro(), cliente.getEndereco().getBairro());
        assertEquals(response.endereco().cep(), cliente.getEndereco().getCep());
        assertEquals(response.endereco().cidade(), cliente.getEndereco().getCidade());
        assertEquals(response.endereco().estado(), cliente.getEndereco().getEstado());
    }

    @Test
    void deveLancarClienteNaoEncontradoExceptionAoConsultarCliente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
            ClienteNaoEncontradoException.class,
            () -> clienteService.findById(1L)
        );

        verify(clienteRepository).findById(1L);
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoConsultarClienteComIdNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> clienteService.findById(null)
        );
    }

}
