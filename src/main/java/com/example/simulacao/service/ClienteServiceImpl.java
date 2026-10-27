package com.example.simulacao.service;

import com.example.simulacao.dto.ClientePatchRequest;
import com.example.simulacao.dto.ClienteRequest;
import com.example.simulacao.dto.ClienteResponse;
import com.example.simulacao.entity.Customer;
import com.example.simulacao.entity.Endereco;
import com.example.simulacao.entity.Estado;
import com.example.simulacao.exception.ClienteNaoEncontradoException;
import com.example.simulacao.exception.ClientePossuiSimulacoesException;
import com.example.simulacao.exception.CpfJaCadastradoException;
import com.example.simulacao.repository.ClienteRepository;
import com.example.simulacao.repository.SimulacaoRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ClienteServiceImpl implements ClienteService {
    private final ClienteRepository clienteRepository;
    private final SimulacaoRepository simulacaoRepository;

    public ClienteServiceImpl(
        ClienteRepository clienteRepository,
        SimulacaoRepository simulacaoRepository
    ) {
        this.clienteRepository = clienteRepository;
        this.simulacaoRepository = simulacaoRepository;
    }

    @Override
    @Transactional
    public ClienteResponse save(ClienteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request não pode ser null");
        }

        if (clienteRepository.findByCpf(request.cpf()).isPresent()) {
            throw new CpfJaCadastradoException("CPF já cadastrado");
        }

        Endereco endereco = Endereco.builder()
            .rua(request.endereco().rua())
            .numero(request.endereco().numero())
            .bairro(request.endereco().bairro())
            .cep(request.endereco().cep())
            .cidade(request.endereco().cidade())
            .estado(request.endereco().estado())
            .build();

        log.info(endereco.toString());

        Customer customer = Customer.builder()
            .cpf(request.cpf())
            .nome(request.nome())
            .endereco(endereco)
            .build();

        log.info(customer.toString());

        clienteRepository.save(customer);

        return ClienteResponse.from(customer);
    }

    @Override
    public ClienteResponse findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id não pode ser null");
        }

        Customer customer = clienteRepository.findById(id)
            .orElseThrow(() ->
                new ClienteNaoEncontradoException("Cliente não encontrado: " + id)
            );

        return ClienteResponse.from(customer);
    }

    @Override
    public Page<ClienteResponse> findAll(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("pageable não pode ser null");
        }

       return clienteRepository
           .findAll(pageable)
           .map(ClienteResponse::from);
    }

    @Override
    @Transactional
    public ClienteResponse patch(Long id, ClientePatchRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("id não pode ser null");
        }

        if (request == null) {
            throw new IllegalArgumentException("request não pode ser null");
        }

        Customer customerExistente = clienteRepository.findById(id)
            .orElseThrow(() ->
                new ClienteNaoEncontradoException("Cliente não encontrado: " + id)
            );

        if (request.cpf() != null) {
            clienteRepository.findByCpf(request.cpf())
                .filter(customer -> !customer.getId().equals(customerExistente.getId()))
                .ifPresent(customer -> {
                    throw new CpfJaCadastradoException("CPF já cadastrado");
                });

            customerExistente.setCpf(request.cpf());
        }

        if (request.nome() != null) {
            customerExistente.setNome(request.nome());
        }

        if (request.endereco() != null) {
            var enderecoRequest = request.endereco();
            var enderecoExistente = customerExistente.getEndereco();

            if (enderecoRequest.rua() != null) {
                enderecoExistente.setRua(enderecoRequest.rua());
            }

            if (enderecoRequest.numero() != null) {
                enderecoExistente.setNumero(enderecoRequest.numero());
            }

            if (enderecoRequest.bairro() != null) {
                enderecoExistente.setBairro(enderecoRequest.bairro());
            }

            if (enderecoRequest.cep() != null) {
                enderecoExistente.setCep(enderecoRequest.cep());
            }

            if (enderecoRequest.cidade() != null) {
                enderecoExistente.setCidade(enderecoRequest.cidade());
            }

            if (enderecoRequest.estado() != null) {
                enderecoExistente.setEstado(enderecoRequest.estado());
            }
        }

        return ClienteResponse.from(customerExistente);
    }

    @Override
    @Transactional
    public ClienteResponse put(Long id, ClienteRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("id não pode ser null");
        }

        if (request == null) {
            throw new IllegalArgumentException("request não pode ser null");
        }

        Customer customerExistente = clienteRepository.findById(id)
            .orElseThrow(() ->
                new ClienteNaoEncontradoException("Cliente não encontrado: " + id)
            );

        clienteRepository.findByCpf(request.cpf())
            // Quando o id é diferente ele mantém na lista
            .filter(clienteCpf -> !clienteCpf.getId().equals(customerExistente.getId()))
            .ifPresent(clienteCpf -> {
                throw new CpfJaCadastradoException("CPF já cadastrado");
            });

        customerExistente.setCpf(request.cpf());
        customerExistente.setNome(request.nome());
        customerExistente.getEndereco().setRua(request.endereco().rua());
        customerExistente.getEndereco().setNumero(request.endereco().numero());
        customerExistente.getEndereco().setBairro(request.endereco().bairro());
        customerExistente.getEndereco().setCep(request.endereco().cep());
        customerExistente.getEndereco().setCidade(request.endereco().cidade());
        customerExistente.getEndereco().setEstado(request.endereco().estado());

        return ClienteResponse.from(customerExistente);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id não pode ser null");
        }

        Customer customerExistente = clienteRepository.findById(id)
            .orElseThrow(() ->
                new ClienteNaoEncontradoException("Cliente não encontrado: " + id)
            );

        if (!simulacaoRepository.findByCustomer(customerExistente, PageRequest.of(0, 1)).isEmpty()) {
            throw new ClientePossuiSimulacoesException("Cliente não pode ser removido pois possui simulações associadas a ele");
        }

        clienteRepository.delete(customerExistente);
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
    public List<String> calculaNomesClientesParaCidadeEstado(
        List<Customer> customers,
        String cidade,
        String estado
    ) {
        return customers.stream()
            .filter(customer -> customer.getEndereco().getCidade().equals(cidade))
            .filter(customer -> customer.getEndereco().getEstado().equals(Estado.valueOf(estado)))
            .map(Customer::getNome)
            .toList();
    }
}
