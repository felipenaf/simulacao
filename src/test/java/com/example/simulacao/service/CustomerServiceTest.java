package com.example.simulacao.service;

import com.example.simulacao.dto.CustomerPatchRequest;
import com.example.simulacao.dto.CustomerRequest;
import com.example.simulacao.dto.CustomerResponse;
import com.example.simulacao.entity.Customer;
import com.example.simulacao.entity.Endereco;
import com.example.simulacao.entity.Estado;
import com.example.simulacao.entity.Simulacao;
import com.example.simulacao.exception.CustomerNaoEncontradoException;
import com.example.simulacao.exception.CustomerPossuiSimulacoesException;
import com.example.simulacao.exception.CpfJaCadastradoException;
import com.example.simulacao.repository.CustomerRepository;
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
    private CustomerRepository customerRepository;

    @Mock
    private SimulacaoRepository simulacaoRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Captor
    private ArgumentCaptor<Customer> customerCaptor;

    private CustomerRequest criarRequest() {
        CustomerRequest.EnderecoRequest endereco = new CustomerRequest.EnderecoRequest(
            "Rua das Flores",
            123,
            "Centro",
            "13000-000",
            "Campinas",
            Estado.SP
        );

        return new CustomerRequest("12345678900", "Felipe Ferreira", endereco);
    }

    private Customer criarCustomer(Long id) {
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
            .name("Felipe Antigo")
            .endereco(endereco)
            .build();
    }

    @Test
    void deveSalvarCustomer() {
        CustomerRequest request = criarRequest();

        when(customerRepository.findByCpf(request.cpf()))
            .thenReturn(Optional.empty());

        CustomerResponse response = customerService.save(request);

        assertNotNull(response);

        // Aqui está sendo interceptado o objeto Customer que é criado dentro
        // da função save do CustomerService e enviado ao repository e não tenho acesso.
        verify(customerRepository).save(customerCaptor.capture());

        // Garante que apenas uma chamada foi feita
        assertEquals(1, customerCaptor.getAllValues().size());

        Customer salvo = customerCaptor.getValue();

        assertEquals(request.cpf(), salvo.getCpf());
        assertEquals(request.nome(), salvo.getName());

        assertEquals(request.endereco().rua(), salvo.getEndereco().getRua());
        assertEquals(request.endereco().numero(), salvo.getEndereco().getNumero());
        assertEquals(request.endereco().bairro(), salvo.getEndereco().getBairro());
        assertEquals(request.endereco().cep(), salvo.getEndereco().getCep());
        assertEquals(request.endereco().cidade(), salvo.getEndereco().getCidade());
        assertEquals(request.endereco().estado(), salvo.getEndereco().getEstado());
    }

    @Test
    void deveRetornarResponseCorretoAoSalvarCustomer() {
        CustomerRequest request = criarRequest();
        when(customerRepository.findByCpf(request.cpf())).thenReturn(Optional.empty());

        CustomerResponse response = customerService.save(request);

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
    void deveLancarCpfJaCadastradoExceptionAoSalvarCustomer() {
        CustomerRequest request = criarRequest();

        when(customerRepository.findByCpf(request.cpf()))
            .thenReturn(Optional.of(Customer.builder().cpf(request.cpf()).build()));

        CpfJaCadastradoException ex = assertThrows(
            CpfJaCadastradoException.class,
            () -> customerService.save(request)
        );

        assertEquals("CPF já cadastrado", ex.getMessage());

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoSalvarCustomerComRequestNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> customerService.save(null)
        );
    }

    @Test
    void deveConsultarCustomer() {
        Customer customer = criarCustomer(1L);

        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(customer));

        CustomerResponse response = customerService.findById(1L);

        verify(customerRepository).findById(1L);

        assertNotNull(response);
        assertEquals(response.cpf(), customer.getCpf());
        assertEquals(response.nome(), customer.getName());
        assertEquals(response.endereco().rua(), customer.getEndereco().getRua());
        assertEquals(response.endereco().numero(), customer.getEndereco().getNumero());
        assertEquals(response.endereco().bairro(), customer.getEndereco().getBairro());
        assertEquals(response.endereco().cep(), customer.getEndereco().getCep());
        assertEquals(response.endereco().cidade(), customer.getEndereco().getCidade());
        assertEquals(response.endereco().estado(), customer.getEndereco().getEstado());
    }

    @Test
    void deveLancarCustomerNaoEncontradoExceptionAoConsultarCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
            CustomerNaoEncontradoException.class,
            () -> customerService.findById(1L)
        );

        verify(customerRepository).findById(1L);
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoConsultarCustomerComIdNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> customerService.findById(null)
        );
    }

    @Test
    void deveConsultarCustomers() {
        List<Customer> customers = IntStream.range(0, 2)
            .mapToObj(i -> criarCustomer((long) i)).toList();

        when(customerRepository.findAll(PageRequest.of(0, 2)))
            .thenReturn(new PageImpl<>(customers));

        Page<CustomerResponse> response = customerService.findAll(PageRequest.of(0, 2));

        verify(customerRepository).findAll(PageRequest.of(0, 2));

        assertNotNull(response);

        assertEquals(response.getContent().getFirst().cpf(), customers.getFirst().getCpf());
        assertEquals(response.getContent().getFirst().nome(), customers.getFirst().getName());
        assertEquals(response.getContent().getFirst().endereco().rua(), customers.getFirst().getEndereco().getRua());
        assertEquals(response.getContent().getFirst().endereco().numero(), customers.getFirst().getEndereco().getNumero());
        assertEquals(response.getContent().getFirst().endereco().bairro(), customers.getFirst().getEndereco().getBairro());
        assertEquals(response.getContent().getFirst().endereco().cep(), customers.getFirst().getEndereco().getCep());
        assertEquals(response.getContent().getFirst().endereco().cidade(), customers.getFirst().getEndereco().getCidade());
        assertEquals(response.getContent().getFirst().endereco().estado(), customers.getFirst().getEndereco().getEstado());

        assertEquals(response.getContent().getLast().cpf(), customers.getLast().getCpf());
        assertEquals(response.getContent().getLast().nome(), customers.getLast().getName());
        assertEquals(response.getContent().getLast().endereco().rua(), customers.getLast().getEndereco().getRua());
        assertEquals(response.getContent().getLast().endereco().numero(), customers.getLast().getEndereco().getNumero());
        assertEquals(response.getContent().getLast().endereco().bairro(), customers.getLast().getEndereco().getBairro());
        assertEquals(response.getContent().getLast().endereco().cep(), customers.getLast().getEndereco().getCep());
        assertEquals(response.getContent().getLast().endereco().cidade(), customers.getLast().getEndereco().getCidade());
        assertEquals(response.getContent().getLast().endereco().estado(), customers.getLast().getEndereco().getEstado());
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoConsultarCustomersComPageableNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> customerService.findAll(null)
        );
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoAtualizarComIdNullPatch() {
        CustomerPatchRequest request = new CustomerPatchRequest(null, "Novo Nome", null);

        assertThrows(
            IllegalArgumentException.class,
            () -> customerService.patch(null, request)
        );

        verifyNoInteractions(customerRepository);
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoAtualizarComRequestNullPatch() {
        assertThrows(
            IllegalArgumentException.class,
            () -> customerService.patch(1L, null)
        );

        verifyNoInteractions(customerRepository);
    }

    @Test
    void deveLancarCustomerNaoEncontradoExceptionAoAtualizarPatch() {
        when(customerRepository.findById(99L))
            .thenReturn(Optional.empty());

        CustomerPatchRequest request = new CustomerPatchRequest(null, "Novo Nome", null);

        assertThrows(
            CustomerNaoEncontradoException.class,
            () -> customerService.patch(99L, request)
        );
    }

    @Test
    void deveAtualizarTodosCamposPatch() {
        Customer existente = criarCustomer(1L);

        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        when(customerRepository.findByCpf("12345678900"))
            .thenReturn(Optional.empty());

        CustomerPatchRequest request = new CustomerPatchRequest(
            "12345678900",
            "João da Silva",
            new CustomerPatchRequest.EnderecoRequest("Rua das Flores", 123, "Centro", "13000-000", "Campinas", Estado.SP)
        );

        CustomerResponse response = customerService.patch(1L, request);

        assertNotNull(response);

        assertEquals("12345678900", existente.getCpf());
        assertEquals("João da Silva", existente.getName());
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
        Customer existente = criarCustomer(1L);

        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        CustomerPatchRequest request = new CustomerPatchRequest(null, "Novo Nome", null);

        customerService.patch(1L, request);

        assertEquals("Novo Nome", existente.getName());
        assertEquals("11111111111", existente.getCpf());
        assertEquals("Rua Antiga", existente.getEndereco().getRua());
        assertEquals("Santos", existente.getEndereco().getCidade());
    }

    @Test
    void deveManterValoresAntigosQuandoTodosCamposNulosAoAtualizar() {
        Customer existente = criarCustomer(1L);

        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        customerService.patch(1L, new CustomerPatchRequest(null, null, null));

        assertEquals("11111111111", existente.getCpf());
        assertEquals("Felipe Antigo", existente.getName());
        assertEquals("Rua Antiga", existente.getEndereco().getRua());

        verify(customerRepository, never()).findByCpf(any());
    }

    @Test
    void devePermitirAtualizarApenasComMesmoCpfDoProprioCustomerPatch() {
        Customer existente = criarCustomer(1L);

        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        when(customerRepository.findByCpf("11111111111"))
            .thenReturn(Optional.of(existente));

        CustomerPatchRequest request = new CustomerPatchRequest("11111111111", null, null);

        CustomerResponse response = customerService.patch(1L, request);

        assertNotNull(response);
        assertEquals("11111111111", existente.getCpf());
    }

    @Test
    void deveLancarCpfJaCadastradoExceptionAoAtualizarPatch() {
        Customer existente = criarCustomer(1L);
        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        Customer outro = criarCustomer(2L);
        when(customerRepository.findByCpf("99999999999"))
            .thenReturn(Optional.of(outro));

        CustomerPatchRequest request = new CustomerPatchRequest("99999999999", "Novo Nome", null);

        assertThrows(
            CpfJaCadastradoException.class,
            () -> customerService.patch(1L, request)
        );

        // a exceção ocorre antes de qualquer alteração
        assertEquals("11111111111", existente.getCpf());
        assertEquals("Felipe Antigo", existente.getName());
    }

    @Test
    void deveAtualizarPut() {
        Customer existente = criarCustomer(1L);
        CustomerRequest request = criarRequest();

        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        when(customerRepository.findByCpf(request.cpf()))
            .thenReturn(Optional.empty());

        CustomerResponse response = customerService.put(1L, request);

        assertNotNull(response);
        assertEquals(request.cpf(), existente.getCpf());
        assertEquals(request.nome(), existente.getName());
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
        CustomerRequest request = criarRequest();

        assertThrows(
            IllegalArgumentException.class,
            () -> customerService.put(null, request)
        );

        verifyNoInteractions(customerRepository);
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoAtualizarQuandoRequestNullPut() {
        assertThrows(
            IllegalArgumentException.class,
            () -> customerService.put(1L, null)
        );

        verifyNoInteractions(customerRepository);
    }

    @Test
    void deveLancarCustomerNaoEncontradoExceptionAoAtualizarPut() {
        when(customerRepository.findById(99L))
            .thenReturn(Optional.empty());

        CustomerRequest request = criarRequest();

        assertThrows(
            CustomerNaoEncontradoException.class,
            () -> customerService.put(99L, request)
        );
    }

    @Test
    void devePermitirAtualizarApenasComMesmoCpfDoProprioCustomerPut() {
        Customer existente = criarCustomer(1L);

        CustomerRequest request = new CustomerRequest(
            "11111111112",
            "Novo Nome",
            criarRequest().endereco()
        );

        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        // o stub precisa ter o mesmo cpf passado na função a ser testada
        // neste exemplo é customerService.put(1L, request);
        when(customerRepository.findByCpf(request.cpf()))
            .thenReturn(Optional.of(existente));

        CustomerResponse response = customerService.put(1L, request);

        assertNotNull(response);
        assertEquals("Novo Nome", existente.getName());
        assertEquals("11111111112", existente.getCpf());
    }

    @Test
    void deveLancarCpfJaCadastradoExceptionAoAtualizarPut() {
        Customer existente = criarCustomer(1L);
        Customer outro = criarCustomer(2L);

        CustomerRequest request = criarRequest();
        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        when(customerRepository.findByCpf(request.cpf()))
            .thenReturn(Optional.of(outro));

        assertThrows(
            CpfJaCadastradoException.class,
            () -> customerService.put(1L, request)
        );

        assertEquals("11111111111", existente.getCpf());
        assertEquals("Felipe Antigo", existente.getName());
        assertEquals("Rua Antiga", existente.getEndereco().getRua());
    }

    @Test
    void deveRemoverCustomer() {
        Customer existente = criarCustomer(1L);

        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        when(simulacaoRepository.findByCustomer(existente, PageRequest.of(0, 1)))
            .thenReturn(Page.empty());

        customerService.delete(1L);

        verify(customerRepository).delete(existente);
    }

    @Test
    void deveLancarIllegalArgumentExceptionAoRemoverQuandoIdNull() {
        assertThrows(
            IllegalArgumentException.class,
            () -> customerService.delete(null)
        );

        verifyNoInteractions(customerRepository, simulacaoRepository);
    }

    @Test
    void deveLancarCustomerNaoEncontradoExceptionAoRemover() {
        when(customerRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(
            CustomerNaoEncontradoException.class,
            () -> customerService.delete(99L)
        );

        verifyNoInteractions(simulacaoRepository);

        verify(customerRepository, never())
            .delete(any(Customer.class));
    }

    @Test
    void deveLancarCustomerPossuiSimulacoesExceptionAoRemover() {
        Customer existente = criarCustomer(1L);
        // Poderia ser new Simualacao() ao invés de mock(Simulacao.class)
        Page<Simulacao> comSimulacao = new PageImpl<>(List.of(mock(Simulacao.class)));

        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        when(simulacaoRepository.findByCustomer(existente, PageRequest.of(0, 1)))
            .thenReturn(comSimulacao);

        assertThrows(
            CustomerPossuiSimulacoesException.class,
            () -> customerService.delete(1L)
        );

        verify(customerRepository, never()).delete(any(Customer.class));
    }

    @Test
    void deveCalcularNomesCustomersParaCidadeEstado() {
        var cli1 = Customer.builder()
            .name("Bruno Oliveira")
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
            .name("Ana Souza")
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
            .name("Carla Mendes")
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

        List<String> nomes = customerService.calculaNomesCustomersParaCidadeEstado(
            customers, "Belo Horizonte", Estado.MG.name()
        );

        assertEquals(1, nomes.size());
        assertEquals("Bruno Oliveira", nomes.getFirst());
    }
}
