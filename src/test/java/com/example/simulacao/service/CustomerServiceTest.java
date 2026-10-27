package com.example.simulacao.service;

import com.example.simulacao.dto.ClientePatchRequest;
import com.example.simulacao.dto.ClienteRequest;
import com.example.simulacao.dto.ClienteResponse;
import com.example.simulacao.entity.Customer;
import com.example.simulacao.entity.Endereco;
import com.example.simulacao.entity.Estado;
import com.example.simulacao.entity.Simulacao;
import com.example.simulacao.exception.ClienteNaoEncontradoException;
import com.example.simulacao.exception.ClientePossuiSimulacoesException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {
    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private SimulacaoRepository simulacaoRepository;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    @Captor
    private ArgumentCaptor<Customer> clienteCaptor;

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

    private Customer criarCliente(Long id) {
        Endereco endereco = Endereco.builder()
            .rua("Rua Antiga")
            .numero(10)
            .bairro("Bairro Antigo")
            .cep("11111-111")
            .cidade("Santos")
            .estado(Estado.PA)
            .build();

        return Customer.builder()
            .id(id)
            .cpf("11111111111")
            .nome("Felipe Antigo")
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

        Customer salvo = clienteCaptor.getValue();

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
            .thenReturn(Optional.of(Customer.builder().cpf(request.cpf()).build()));

        CpfJaCadastradoException ex = assertThrows(
            CpfJaCadastradoException.class,
            () -> clienteService.save(request)
        );

        assertEquals("CPF já cadastrado", ex.getMessage());

        verify(clienteRepository, never()).save(any(Customer.class));
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
        Customer customer = criarCliente(1L);

        when(clienteRepository.findById(1L))
            .thenReturn(Optional.of(customer));

        ClienteResponse response = clienteService.findById(1L);

        verify(clienteRepository).findById(1L);

        assertNotNull(response);
        assertEquals(response.cpf(), customer.getCpf());
        assertEquals(response.nome(), customer.getNome());
        assertEquals(response.endereco().rua(), customer.getEndereco().getRua());
        assertEquals(response.endereco().numero(), customer.getEndereco().getNumero());
        assertEquals(response.endereco().bairro(), customer.getEndereco().getBairro());
        assertEquals(response.endereco().cep(), customer.getEndereco().getCep());
        assertEquals(response.endereco().cidade(), customer.getEndereco().getCidade());
        assertEquals(response.endereco().estado(), customer.getEndereco().getEstado());
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

    @Test
    void deveConsultarClientes() {
        List<Customer> customers = IntStream.range(0, 2)
            .mapToObj(i -> criarCliente((long) i)).toList();

        when(clienteRepository.findAll(PageRequest.of(0, 2)))
            .thenReturn(new PageImpl<>(customers));

        Page<ClienteResponse> response = clienteService.findAll(PageRequest.of(0, 2));

        verify(clienteRepository).findAll(PageRequest.of(0, 2));

        assertNotNull(response);

        assertEquals(response.getContent().getFirst().cpf(), customers.getFirst().getCpf());
        assertEquals(response.getContent().getFirst().nome(), customers.getFirst().getNome());
        assertEquals(response.getContent().getFirst().endereco().rua(), customers.getFirst().getEndereco().getRua());
        assertEquals(response.getContent().getFirst().endereco().numero(), customers.getFirst().getEndereco().getNumero());
        assertEquals(response.getContent().getFirst().endereco().bairro(), customers.getFirst().getEndereco().getBairro());
        assertEquals(response.getContent().getFirst().endereco().cep(), customers.getFirst().getEndereco().getCep());
        assertEquals(response.getContent().getFirst().endereco().cidade(), customers.getFirst().getEndereco().getCidade());
        assertEquals(response.getContent().getFirst().endereco().estado(), customers.getFirst().getEndereco().getEstado());

        assertEquals(response.getContent().getLast().cpf(), customers.getLast().getCpf());
        assertEquals(response.getContent().getLast().nome(), customers.getLast().getNome());
        assertEquals(response.getContent().getLast().endereco().rua(), customers.getLast().getEndereco().getRua());
        assertEquals(response.getContent().getLast().endereco().numero(), customers.getLast().getEndereco().getNumero());
        assertEquals(response.getContent().getLast().endereco().bairro(), customers.getLast().getEndereco().getBairro());
        assertEquals(response.getContent().getLast().endereco().cep(), customers.getLast().getEndereco().getCep());
        assertEquals(response.getContent().getLast().endereco().cidade(), customers.getLast().getEndereco().getCidade());
        assertEquals(response.getContent().getLast().endereco().estado(), customers.getLast().getEndereco().getEstado());
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoConsultarClientesComPageableNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> clienteService.findAll(null)
        );
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoAtualizarComIdNullPatch() {
        ClientePatchRequest request = new ClientePatchRequest(null, "Novo Nome", null);

        assertThrows(
            IllegalArgumentException.class,
            () -> clienteService.patch(null, request)
        );

        verifyNoInteractions(clienteRepository);
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoAtualizarComRequestNullPatch() {
        assertThrows(
            IllegalArgumentException.class,
            () -> clienteService.patch(1L, null)
        );

        verifyNoInteractions(clienteRepository);
    }

    @Test
    void deveLancarClienteNaoEncontradoExceptionAoAtualizarPatch() {
        when(clienteRepository.findById(99L))
            .thenReturn(Optional.empty());

        ClientePatchRequest request = new ClientePatchRequest(null, "Novo Nome", null);

        assertThrows(
            ClienteNaoEncontradoException.class,
            () -> clienteService.patch(99L, request)
        );
    }

    @Test
    void deveAtualizarTodosCamposPatch() {
        Customer existente = criarCliente(1L);

        when(clienteRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        when(clienteRepository.findByCpf("12345678900"))
            .thenReturn(Optional.empty());

        ClientePatchRequest request = new ClientePatchRequest(
            "12345678900",
            "João da Silva",
            new ClientePatchRequest.EnderecoRequest("Rua das Flores", 123, "Centro", "13000-000", "Campinas", Estado.SP)
        );

        ClienteResponse response = clienteService.patch(1L, request);

        assertNotNull(response);

        assertEquals("12345678900", existente.getCpf());
        assertEquals("João da Silva", existente.getNome());
        assertEquals("Rua das Flores", existente.getEndereco().getRua());
        assertEquals(123, existente.getEndereco().getNumero());
        assertEquals("Centro", existente.getEndereco().getBairro());
        assertEquals("13000000", existente.getEndereco().getCep());
        assertEquals("Campinas", existente.getEndereco().getCidade());
        assertEquals(Estado.SP, existente.getEndereco().getEstado());

        assertEquals("12345678900", response.cpf());
        assertEquals("João da Silva", response.nome());
    }

    @Test
    void deveAtualizarApenasNomePatch() {
        Customer existente = criarCliente(1L);

        when(clienteRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        ClientePatchRequest request = new ClientePatchRequest(null, "Novo Nome", null);

        clienteService.patch(1L, request);

        assertEquals("Novo Nome", existente.getNome());
        assertEquals("11111111111", existente.getCpf());
        assertEquals("Rua Antiga", existente.getEndereco().getRua());
        assertEquals("Santos", existente.getEndereco().getCidade());
    }

    @Test
    void deveManterValoresAntigosQuandoTodosCamposNulosAoAtualizar() {
        Customer existente = criarCliente(1L);

        when(clienteRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        clienteService.patch(1L, new ClientePatchRequest(null, null, null));

        assertEquals("11111111111", existente.getCpf());
        assertEquals("Felipe Antigo", existente.getNome());
        assertEquals("Rua Antiga", existente.getEndereco().getRua());

        verify(clienteRepository, never()).findByCpf(any());
    }

    @Test
    void devePermitirAtualizarApenasComMesmoCpfDoProprioClientePatch() {
        Customer existente = criarCliente(1L);

        when(clienteRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        when(clienteRepository.findByCpf("11111111111"))
            .thenReturn(Optional.of(existente));

        ClientePatchRequest request = new ClientePatchRequest("11111111111", null, null);

        ClienteResponse response = clienteService.patch(1L, request);

        assertNotNull(response);
        assertEquals("11111111111", existente.getCpf());
    }

    @Test
    void deveLancarCpfJaCadastradoExceptionAoAtualizarPatch() {
        Customer existente = criarCliente(1L);
        when(clienteRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        Customer outro = criarCliente(2L);
        when(clienteRepository.findByCpf("99999999999"))
            .thenReturn(Optional.of(outro));

        ClientePatchRequest request = new ClientePatchRequest("99999999999", "Novo Nome", null);

        assertThrows(
            CpfJaCadastradoException.class,
            () -> clienteService.patch(1L, request)
        );

        // a exceção ocorre antes de qualquer alteração
        assertEquals("11111111111", existente.getCpf());
        assertEquals("Felipe Antigo", existente.getNome());
    }

    @Test
    void deveAtualizarPut() {
        Customer existente = criarCliente(1L);
        ClienteRequest request = criarRequest();

        when(clienteRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        when(clienteRepository.findByCpf(request.cpf()))
            .thenReturn(Optional.empty());

        ClienteResponse response = clienteService.put(1L, request);

        assertNotNull(response);
        assertEquals(request.cpf(), existente.getCpf());
        assertEquals(request.nome(), existente.getNome());
        assertEquals(request.endereco().rua(), existente.getEndereco().getRua());
        assertEquals(request.endereco().numero(), existente.getEndereco().getNumero());
        assertEquals(request.endereco().bairro(), existente.getEndereco().getBairro());
        assertEquals(request.endereco().cep(), existente.getEndereco().getCep());
        assertEquals(request.endereco().cidade(), existente.getEndereco().getCidade());
        assertEquals(request.endereco().estado(), existente.getEndereco().getEstado());

        assertEquals(request.cpf(), response.cpf());
        assertEquals(request.nome(), response.nome());
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoAtualizarQuandoIdNullPut() {
        ClienteRequest request = criarRequest();

        assertThrows(
            IllegalArgumentException.class,
            () -> clienteService.put(null, request)
        );

        verifyNoInteractions(clienteRepository);
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoAtualizarQuandoRequestNullPut() {
        assertThrows(
            IllegalArgumentException.class,
            () -> clienteService.put(1L, null)
        );

        verifyNoInteractions(clienteRepository);
    }

    @Test
    void deveLancarClienteNaoEncontradoExceptionAoAtualizarPut() {
        when(clienteRepository.findById(99L))
            .thenReturn(Optional.empty());

        ClienteRequest request = criarRequest();

        assertThrows(
            ClienteNaoEncontradoException.class,
            () -> clienteService.put(99L, request)
        );
    }

    @Test
    void devePermitirAtualizarApenasComMesmoCpfDoProprioClientePut() {
        Customer existente = criarCliente(1L);

        ClienteRequest request = new ClienteRequest(
            "11111111112",
            "Novo Nome",
            criarRequest().endereco()
        );

        when(clienteRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        // o stub precisa ter o mesmo cpf passado na função a ser testada
        // neste exemplo é clienteService.put(1L, request);
        when(clienteRepository.findByCpf(request.cpf()))
            .thenReturn(Optional.of(existente));

        ClienteResponse response = clienteService.put(1L, request);

        assertNotNull(response);
        assertEquals("Novo Nome", existente.getNome());
        assertEquals("11111111112", existente.getCpf());
    }

    @Test
    void deveLancarCpfJaCadastradoExceptionAoAtualizarPut() {
        Customer existente = criarCliente(1L);
        Customer outro = criarCliente(2L);

        ClienteRequest request = criarRequest();
        when(clienteRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        when(clienteRepository.findByCpf(request.cpf()))
            .thenReturn(Optional.of(outro));

        assertThrows(
            CpfJaCadastradoException.class,
            () -> clienteService.put(1L, request)
        );

        assertEquals("11111111111", existente.getCpf());
        assertEquals("Felipe Antigo", existente.getNome());
        assertEquals("Rua Antiga", existente.getEndereco().getRua());
    }

    @Test
    void deveRemoverCliente() {
        Customer existente = criarCliente(1L);

        when(clienteRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        when(simulacaoRepository.findByCustomer(existente, PageRequest.of(0, 1)))
            .thenReturn(Page.empty());

        clienteService.delete(1L);

        verify(clienteRepository).delete(existente);
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoRemoverQuandoIdNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> clienteService.delete(null)
        );

        verifyNoInteractions(clienteRepository, simulacaoRepository);
    }

    @Test
    void deveLancarClienteNaoEncontradoExceptionAoRemover() {
        when(clienteRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(
            ClienteNaoEncontradoException.class,
            () -> clienteService.delete(99L)
        );

        verifyNoInteractions(simulacaoRepository);

        verify(clienteRepository, never())
            .delete(any(Customer.class));
    }

    @Test
    void deveLancarClientePossuiSimulacoesExceptionAoRemover() {
        Customer existente = criarCliente(1L);
        // Poderia ser new Simualacao() ao invés de mock(Simulacao.class)
        Page<Simulacao> comSimulacao = new PageImpl<>(List.of(mock(Simulacao.class)));

        when(clienteRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        when(simulacaoRepository.findByCustomer(existente, PageRequest.of(0, 1)))
            .thenReturn(comSimulacao);

        assertThrows(
            ClientePossuiSimulacoesException.class,
            () -> clienteService.delete(1L)
        );

        verify(clienteRepository, never()).delete(any(Customer.class));
    }

    @Test
    void deveCalcularNomesClientesParaCidadeEstado() {
        var cli1 = Customer.builder()
            .nome("Bruno Oliveira")
            .cpf("98765432100")
            .endereco(Endereco.builder()
                .rua("Rua da Bahia")
                .numero(1148)
                .bairro("Centro")
                .cep("30160011")
                .cidade("Belo Horizonte")
                .estado(Estado.MG)
                .build())
            .build();

        var cli2 = Customer.builder()
            .nome("Ana Souza")
            .cpf("12345678909")
            .endereco(Endereco.builder()
                .rua("Avenida Paulista")
                .numero(1578)
                .bairro("Bela Vista")
                .cep("01310200")
                .cidade("São Paulo")
                .estado(Estado.SP)
                .build())
            .build();

        var cli3 = Customer.builder()
            .nome("Carla Mendes")
            .cpf("45678912311")
            .endereco(Endereco.builder()
                .rua("Rua Voluntários da Pátria")
                .numero(45)
                .bairro("Botafogo")
                .cep("22270000")
                .cidade("Rio de Janeiro")
                .estado(Estado.RJ)
                .build())
            .build();

        List<Customer> customers = List.of(cli1, cli2, cli3);

        List<String> nomes = clienteService.calculaNomesClientesParaCidadeEstado(
            customers, "Belo Horizonte", Estado.MG.name()
        );

        assertEquals(1, nomes.size());
        assertEquals("Bruno Oliveira", nomes.getFirst());
    }
}
