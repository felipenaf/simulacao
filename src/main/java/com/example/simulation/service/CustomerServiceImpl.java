package com.example.simulation.service;

import com.example.simulation.dto.CustomerPatchRequest;
import com.example.simulation.dto.CustomerRequest;
import com.example.simulation.dto.CustomerResponse;
import com.example.simulation.entity.Customer;
import com.example.simulation.entity.Address;
import com.example.simulation.entity.State;
import com.example.simulation.exception.CustomerNaoEncontradoException;
import com.example.simulation.exception.CustomerPossuiSimulacoesException;
import com.example.simulation.exception.CpfJaCadastradoException;
import com.example.simulation.repository.CustomerRepository;
import com.example.simulation.repository.SimulationRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final SimulationRepository simulationRepository;

    public CustomerServiceImpl(
        CustomerRepository customerRepository,
        SimulationRepository simulationRepository
    ) {
        this.customerRepository = customerRepository;
        this.simulationRepository = simulationRepository;
    }

    @Override
    @Transactional
    public CustomerResponse save(CustomerRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request não pode ser null");
        }

        if (customerRepository.findByCpf(request.cpf()).isPresent()) {
            throw new CpfJaCadastradoException("CPF já cadastrado");
        }

        Address address = Address.builder()
            .rua(request.address().rua())
            .numero(request.address().numero())
            .bairro(request.address().bairro())
            .cep(request.address().cep())
            .cidade(request.address().cidade())
            .state(request.address().state())
            .build();

        log.info(address.toString());

        Customer customer = Customer.builder()
            .cpf(request.cpf())
            .name(request.name())
            .address(address)
            .build();

        log.info(customer.toString());

        customerRepository.save(customer);

        return CustomerResponse.from(customer);
    }

    @Override
    public CustomerResponse findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id não pode ser null");
        }

        Customer customer = customerRepository.findById(id)
            .orElseThrow(() ->
                new CustomerNaoEncontradoException("Customer não encontrado: " + id)
            );

        return CustomerResponse.from(customer);
    }

    @Override
    public Page<CustomerResponse> findAll(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("pageable não pode ser null");
        }

       return customerRepository
           .findAll(pageable)
           .map(CustomerResponse::from);
    }

    @Override
    @Transactional
    public CustomerResponse patch(Long id, CustomerPatchRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("id não pode ser null");
        }

        if (request == null) {
            throw new IllegalArgumentException("request não pode ser null");
        }

        Customer existingCustomer = customerRepository.findById(id)
            .orElseThrow(() ->
                new CustomerNaoEncontradoException("Customer não encontrado: " + id)
            );

        if (request.cpf() != null) {
            customerRepository.findByCpf(request.cpf())
                .filter(customer -> !customer.getId().equals(existingCustomer.getId()))
                .ifPresent(customer -> {
                    throw new CpfJaCadastradoException("CPF já cadastrado");
                });

            existingCustomer.setCpf(request.cpf());
        }

        if (request.name() != null) {
            existingCustomer.setName(request.name());
        }

        if (request.address() != null) {
            if (request.address().rua() != null) {
                existingCustomer.getAddress()
                    .setRua(request.address().rua());
            }

            if (request.address().numero() != null) {
                existingCustomer.getAddress()
                    .setNumero(request.address().numero());
            }

            if (request.address().bairro() != null) {
                existingCustomer.getAddress()
                    .setBairro(request.address().bairro());
            }

            if (request.address().cep() != null) {
                existingCustomer.getAddress()
                    .setCep(request.address().cep());
            }

            if (request.address().cidade() != null) {
                existingCustomer.getAddress()
                    .setCidade(request.address().cidade());
            }

            if (request.address().state() != null) {
                existingCustomer.getAddress()
                    .setState(request.address().state());
            }
        }

        return CustomerResponse.from(existingCustomer);
    }

    @Override
    @Transactional
    public CustomerResponse put(Long id, CustomerRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("id não pode ser null");
        }

        if (request == null) {
            throw new IllegalArgumentException("request não pode ser null");
        }

        Customer customerExistente = customerRepository.findById(id)
            .orElseThrow(() ->
                new CustomerNaoEncontradoException("Customer não encontrado: " + id)
            );

        customerRepository.findByCpf(request.cpf())
            // Quando o id é diferente ele mantém na lista
            .filter(customerCpf -> !customerCpf.getId().equals(customerExistente.getId()))
            .ifPresent(customerCpf -> {
                throw new CpfJaCadastradoException("CPF já cadastrado");
            });

        customerExistente.setCpf(request.cpf());
        customerExistente.setName(request.name());
        customerExistente.getAddress().setRua(request.address().rua());
        customerExistente.getAddress().setNumero(request.address().numero());
        customerExistente.getAddress().setBairro(request.address().bairro());
        customerExistente.getAddress().setCep(request.address().cep());
        customerExistente.getAddress().setCidade(request.address().cidade());
        customerExistente.getAddress().setState(request.address().state());

        return CustomerResponse.from(customerExistente);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id não pode ser null");
        }

        Customer customerExistente = customerRepository.findById(id)
            .orElseThrow(() ->
                new CustomerNaoEncontradoException("Customer não encontrado: " + id)
            );

        if (!simulationRepository.findByCustomer(customerExistente, PageRequest.of(0, 1)).isEmpty()) {
            throw new CustomerPossuiSimulacoesException("Customer não pode ser removido pois possui simulações associadas a ele");
        }

        customerRepository.delete(customerExistente);
    }

    /**
     * TODO: Pass this to Repository
     * Calcula uma lista com os nomes dos customers que moram em uma determinada cidade e estado.
     *
     * @param customers lista dos customers usados
    como base para o cálculo
     * @param cidade cidade dos customers a serem
    recuperados
     * @param estado estado da cidade dos customers
    a serem recuperados
     * @return Lista com os nomes dos customers
    da lista fornecida que moram na cidade e estado fornecida.
     */
    public List<String> calculaNomesCustomersParaCidadeEstado(
        List<Customer> customers,
        String cidade,
        String estado
    ) {
        return customers.stream()
            .filter(customer -> customer.getAddress().getCidade().equals(cidade))
            .filter(customer -> customer.getAddress().getState().equals(State.valueOf(estado)))
            .map(Customer::getName)
            .toList();
    }
}
