package com.example.simulation.service;

import com.example.simulation.dto.CustomerPatchRequest;
import com.example.simulation.dto.CustomerRequest;
import com.example.simulation.dto.CustomerResponse;
import com.example.simulation.entity.Customer;
import com.example.simulation.entity.Address;
import com.example.simulation.entity.State;
import com.example.simulation.exception.CustomerNotFoundException;
import com.example.simulation.exception.CustomerHaveSimulationsException;
import com.example.simulation.exception.CpfAlreadyAssociatedException;
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
            throw new CpfAlreadyAssociatedException("CPF já cadastrado");
        }

        Address address = Address.builder()
            .street(request.address().street())
            .number(request.address().number())
            .neighborhood(request.address().neighborhood())
            .postalCode(request.address().postalCode())
            .city(request.address().city())
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
                new CustomerNotFoundException("Customer não encontrado: " + id)
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
                new CustomerNotFoundException("Customer não encontrado: " + id)
            );

        if (request.cpf() != null) {
            customerRepository.findByCpf(request.cpf())
                .filter(customer -> !customer.getId().equals(existingCustomer.getId()))
                .ifPresent(customer -> {
                    throw new CpfAlreadyAssociatedException("CPF já cadastrado");
                });

            existingCustomer.setCpf(request.cpf());
        }

        if (request.name() != null) {
            existingCustomer.setName(request.name());
        }

        if (request.address() != null) {
            if (request.address().street() != null) {
                existingCustomer.getAddress()
                    .setStreet(request.address().street());
            }

            if (request.address().number() != null) {
                existingCustomer.getAddress()
                    .setNumber(request.address().number());
            }

            if (request.address().neighborhood() != null) {
                existingCustomer.getAddress()
                    .setNeighborhood(request.address().neighborhood());
            }

            if (request.address().postalCode() != null) {
                existingCustomer.getAddress()
                    .setPostalCode(request.address().postalCode());
            }

            if (request.address().city() != null) {
                existingCustomer.getAddress()
                    .setCity(request.address().city());
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
                new CustomerNotFoundException("Customer não encontrado: " + id)
            );

        customerRepository.findByCpf(request.cpf())
            // Quando o id é diferente ele mantém na lista
            .filter(customerCpf -> !customerCpf.getId().equals(customerExistente.getId()))
            .ifPresent(customerCpf -> {
                throw new CpfAlreadyAssociatedException("CPF já cadastrado");
            });

        customerExistente.setCpf(request.cpf());
        customerExistente.setName(request.name());
        customerExistente.getAddress().setStreet(request.address().street());
        customerExistente.getAddress().setNumber(request.address().number());
        customerExistente.getAddress().setNeighborhood(request.address().neighborhood());
        customerExistente.getAddress().setPostalCode(request.address().postalCode());
        customerExistente.getAddress().setCity(request.address().city());
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
                new CustomerNotFoundException("Customer não encontrado: " + id)
            );

        if (!simulationRepository.findByCustomer(customerExistente, PageRequest.of(0, 1)).isEmpty()) {
            throw new CustomerHaveSimulationsException("Customer não pode ser removido pois possui simulações associadas a ele");
        }

        customerRepository.delete(customerExistente);
    }

    /**
     * TODO: Pass this to Repository
     * Calcula uma lista com os nomes dos customers que moram em uma determinada city e estado.
     *
     * @param customers lista dos customers usados
    como base para o cálculo
     * @param cidade city dos customers a serem
    recuperados
     * @param estado estado da city dos customers
    a serem recuperados
     * @return Lista com os nomes dos customers
    da lista fornecida que moram na city e estado fornecida.
     */
    public List<String> calculaNomesCustomersParaCidadeEstado(
        List<Customer> customers,
        String cidade,
        String estado
    ) {
        return customers.stream()
            .filter(customer -> customer.getAddress().getCity().equals(cidade))
            .filter(customer -> customer.getAddress().getState().equals(State.valueOf(estado)))
            .map(Customer::getName)
            .toList();
    }
}
