package com.example.simulation.service;

import com.example.simulation.dto.CustomerPatchRequest;
import com.example.simulation.dto.CustomerRequest;
import com.example.simulation.dto.CustomerResponse;
import com.example.simulation.entity.*;
import com.example.simulation.exception.CustomerNaoEncontradoException;
import com.example.simulation.exception.CustomerHaveSimulationsException;
import com.example.simulation.exception.CpfAlreadyAssociatedException;
import com.example.simulation.repository.CustomerRepository;
import com.example.simulation.repository.SimulationRepository;
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
    private SimulationRepository simulationRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Captor
    private ArgumentCaptor<Customer> customerCaptor;

    private CustomerRequest criarRequest() {
        CustomerRequest.AddressRequest address = new CustomerRequest.AddressRequest(
            "Rua das Flores",
            123,
            "Centro",
            "13000-000",
            "Campinas",
            State.SP
        );

        return new CustomerRequest("12345678900", "Felipe Ferreira", address);
    }

    private Customer criarCustomer(Long id) {
        Address address = Address.builder()
            .rua("Rua Antiga")
            .numero(10)
            .bairro("Bairro Antigo")
            .cep("11111-111")
            .cidade("Santos")
            .state(State.PA)
            .build();

        return Customer.builder()
            .id(id)
            .cpf("11111111111")
            .name("Felipe Antigo")
            .address(address)
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
        assertEquals(request.name(), salvo.getName());

        assertEquals(request.address().rua(), salvo.getAddress().getRua());
        assertEquals(request.address().numero(), salvo.getAddress().getNumero());
        assertEquals(request.address().bairro(), salvo.getAddress().getBairro());
        assertEquals(request.address().cep(), salvo.getAddress().getCep());
        assertEquals(request.address().cidade(), salvo.getAddress().getCidade());
        assertEquals(request.address().state(), salvo.getAddress().getState());
    }

    @Test
    void deveRetornarResponseCorretoAoSalvarCustomer() {
        CustomerRequest request = criarRequest();
        when(customerRepository.findByCpf(request.cpf())).thenReturn(Optional.empty());

        CustomerResponse response = customerService.save(request);

        assertEquals(request.cpf(), response.cpf());
        assertEquals(request.name(), response.name());
        assertEquals(request.address().rua(), response.address().rua());
        assertEquals(request.address().numero(), response.address().numero());
        assertEquals(request.address().bairro(), response.address().bairro());
        assertEquals(request.address().cep(), response.address().cep());
        assertEquals(request.address().cidade(), response.address().cidade());
        assertEquals(request.address().state(), response.address().state());
    }

    @Test
    void deveLancarCpfJaCadastradoExceptionAoSalvarCustomer() {
        CustomerRequest request = criarRequest();

        when(customerRepository.findByCpf(request.cpf()))
            .thenReturn(Optional.of(Customer.builder().cpf(request.cpf()).build()));

        CpfAlreadyAssociatedException ex = assertThrows(
            CpfAlreadyAssociatedException.class,
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
        assertEquals(response.name(), customer.getName());
        assertEquals(response.address().rua(), customer.getAddress().getRua());
        assertEquals(response.address().numero(), customer.getAddress().getNumero());
        assertEquals(response.address().bairro(), customer.getAddress().getBairro());
        assertEquals(response.address().cep(), customer.getAddress().getCep());
        assertEquals(response.address().cidade(), customer.getAddress().getCidade());
        assertEquals(response.address().state(), customer.getAddress().getState());
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
        assertEquals(response.getContent().getFirst().name(), customers.getFirst().getName());
        assertEquals(response.getContent().getFirst().address().rua(), customers.getFirst().getAddress().getRua());
        assertEquals(response.getContent().getFirst().address().numero(), customers.getFirst().getAddress().getNumero());
        assertEquals(response.getContent().getFirst().address().bairro(), customers.getFirst().getAddress().getBairro());
        assertEquals(response.getContent().getFirst().address().cep(), customers.getFirst().getAddress().getCep());
        assertEquals(response.getContent().getFirst().address().cidade(), customers.getFirst().getAddress().getCidade());
        assertEquals(response.getContent().getFirst().address().state(), customers.getFirst().getAddress().getState());

        assertEquals(response.getContent().getLast().cpf(), customers.getLast().getCpf());
        assertEquals(response.getContent().getLast().name(), customers.getLast().getName());
        assertEquals(response.getContent().getLast().address().rua(), customers.getLast().getAddress().getRua());
        assertEquals(response.getContent().getLast().address().numero(), customers.getLast().getAddress().getNumero());
        assertEquals(response.getContent().getLast().address().bairro(), customers.getLast().getAddress().getBairro());
        assertEquals(response.getContent().getLast().address().cep(), customers.getLast().getAddress().getCep());
        assertEquals(response.getContent().getLast().address().cidade(), customers.getLast().getAddress().getCidade());
        assertEquals(response.getContent().getLast().address().state(), customers.getLast().getAddress().getState());
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
            new CustomerPatchRequest.AddressRequest("Rua das Flores", 123, "Centro", "13000-000", "Campinas", State.SP)
        );

        CustomerResponse response = customerService.patch(1L, request);

        assertNotNull(response);

        assertEquals("12345678900", existente.getCpf());
        assertEquals("João da Silva", existente.getName());
        assertEquals("Rua das Flores", existente.getAddress().getRua());
        assertEquals(123, existente.getAddress().getNumero());
        assertEquals("Centro", existente.getAddress().getBairro());
        assertEquals("13000000", existente.getAddress().getCep());
        assertEquals("Campinas", existente.getAddress().getCidade());
        assertEquals(State.SP, existente.getAddress().getState());

        assertEquals("12345678900", response.cpf());
        assertEquals("João da Silva", response.name());
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
        assertEquals("Rua Antiga", existente.getAddress().getRua());
        assertEquals("Santos", existente.getAddress().getCidade());
    }

    @Test
    void deveManterValoresAntigosQuandoTodosCamposNulosAoAtualizar() {
        Customer existente = criarCustomer(1L);

        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        customerService.patch(1L, new CustomerPatchRequest(null, null, null));

        assertEquals("11111111111", existente.getCpf());
        assertEquals("Felipe Antigo", existente.getName());
        assertEquals("Rua Antiga", existente.getAddress().getRua());

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
            CpfAlreadyAssociatedException.class,
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
        assertEquals(request.name(), existente.getName());
        assertEquals(request.address().rua(), existente.getAddress().getRua());
        assertEquals(request.address().numero(), existente.getAddress().getNumero());
        assertEquals(request.address().bairro(), existente.getAddress().getBairro());
        assertEquals(request.address().cep(), existente.getAddress().getCep());
        assertEquals(request.address().cidade(), existente.getAddress().getCidade());
        assertEquals(request.address().state(), existente.getAddress().getState());

        assertEquals(request.cpf(), response.cpf());
        assertEquals(request.name(), response.name());
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
            criarRequest().address()
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
            CpfAlreadyAssociatedException.class,
            () -> customerService.put(1L, request)
        );

        assertEquals("11111111111", existente.getCpf());
        assertEquals("Felipe Antigo", existente.getName());
        assertEquals("Rua Antiga", existente.getAddress().getRua());
    }

    @Test
    void deveRemoverCustomer() {
        Customer existente = criarCustomer(1L);

        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        when(simulationRepository.findByCustomer(existente, PageRequest.of(0, 1)))
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

        verifyNoInteractions(customerRepository, simulationRepository);
    }

    @Test
    void deveLancarCustomerNaoEncontradoExceptionAoRemover() {
        when(customerRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(
            CustomerNaoEncontradoException.class,
            () -> customerService.delete(99L)
        );

        verifyNoInteractions(simulationRepository);

        verify(customerRepository, never())
            .delete(any(Customer.class));
    }

    @Test
    void deveLancarCustomerPossuiSimulacoesExceptionAoRemover() {
        Customer existente = criarCustomer(1L);
        // Poderia ser new Simualacao() ao invés de mock(Simulacao.class)
        Page<Simulation> comSimulacao = new PageImpl<>(List.of(mock(Simulation.class)));

        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(existente));

        when(simulationRepository.findByCustomer(existente, PageRequest.of(0, 1)))
            .thenReturn(comSimulacao);

        assertThrows(
            CustomerHaveSimulationsException.class,
            () -> customerService.delete(1L)
        );

        verify(customerRepository, never()).delete(any(Customer.class));
    }

    @Test
    void deveCalcularNomesCustomersParaCidadeEstado() {
        var cli1 = Customer.builder()
            .name("Bruno Oliveira")
            .cpf("98765432100")
            .address(Address.builder()
                .rua("Rua da Bahia")
                .numero(1148)
                .bairro("Centro")
                .cep("30160011")
                .cidade("Belo Horizonte")
                .state(State.MG)
                .build())
            .build();

        var cli2 = Customer.builder()
            .name("Ana Souza")
            .cpf("12345678909")
            .address(Address.builder()
                .rua("Avenida Paulista")
                .numero(1578)
                .bairro("Bela Vista")
                .cep("01310200")
                .cidade("São Paulo")
                .state(State.SP)
                .build())
            .build();

        var cli3 = Customer.builder()
            .name("Carla Mendes")
            .cpf("45678912311")
            .address(Address.builder()
                .rua("Rua Voluntários da Pátria")
                .numero(45)
                .bairro("Botafogo")
                .cep("22270000")
                .cidade("Rio de Janeiro")
                .state(State.RJ)
                .build())
            .build();

        List<Customer> customers = List.of(cli1, cli2, cli3);

        List<String> names = customerService.calculaNomesCustomersParaCidadeEstado(
            customers, "Belo Horizonte", State.MG.name()
        );

        assertEquals(1, names.size());
        assertEquals("Bruno Oliveira", names.getFirst());
    }
}
