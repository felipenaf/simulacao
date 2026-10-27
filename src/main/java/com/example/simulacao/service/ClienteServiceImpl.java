package com.example.simulacao.service;

import com.example.simulacao.dto.ClientePatchRequest;
import com.example.simulacao.dto.ClienteRequest;
import com.example.simulacao.dto.ClienteResponse;
import com.example.simulacao.entity.Cliente;
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

        Cliente cliente = Cliente.builder()
            .cpf(request.cpf())
            .nome(request.nome())
            .endereco(endereco)
            .build();

        log.info(cliente.toString());

        clienteRepository.save(cliente);

        return ClienteResponse.from(cliente);
    }

    @Override
    public ClienteResponse findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id não pode ser null");
        }

        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() ->
                new ClienteNaoEncontradoException("Cliente não encontrado: " + id)
            );

        return ClienteResponse.from(cliente);
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

        Cliente clienteExistente = clienteRepository.findById(id)
            .orElseThrow(() ->
                new ClienteNaoEncontradoException("Cliente não encontrado: " + id)
            );

        if (request.cpf() != null) {
            clienteRepository.findByCpf(request.cpf())
                .filter(cliente -> !cliente.getId().equals(clienteExistente.getId()))
                .ifPresent(cliente -> {
                    throw new CpfJaCadastradoException("CPF já cadastrado");
                });

            clienteExistente.setCpf(request.cpf());
        }

        if (request.nome() != null) {
            clienteExistente.setNome(request.nome());
        }

        if (request.endereco() != null) {
            var enderecoRequest = request.endereco();
            var enderecoExistente = clienteExistente.getEndereco();

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

        return ClienteResponse.from(clienteExistente);
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

        Cliente clienteExistente = clienteRepository.findById(id)
            .orElseThrow(() ->
                new ClienteNaoEncontradoException("Cliente não encontrado: " + id)
            );

        clienteRepository.findByCpf(request.cpf())
            // Quando o id é diferente ele mantém na lista
            .filter(clienteCpf -> !clienteCpf.getId().equals(clienteExistente.getId()))
            .ifPresent(clienteCpf -> {
                throw new CpfJaCadastradoException("CPF já cadastrado");
            });

        clienteExistente.setCpf(request.cpf());
        clienteExistente.setNome(request.nome());
        clienteExistente.getEndereco().setRua(request.endereco().rua());
        clienteExistente.getEndereco().setNumero(request.endereco().numero());
        clienteExistente.getEndereco().setBairro(request.endereco().bairro());
        clienteExistente.getEndereco().setCep(request.endereco().cep());
        clienteExistente.getEndereco().setCidade(request.endereco().cidade());
        clienteExistente.getEndereco().setEstado(request.endereco().estado());

        return ClienteResponse.from(clienteExistente);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id não pode ser null");
        }

        Cliente clienteExistente = clienteRepository.findById(id)
            .orElseThrow(() ->
                new ClienteNaoEncontradoException("Cliente não encontrado: " + id)
            );

        if (!simulacaoRepository.findByCliente(clienteExistente, PageRequest.of(0, 1)).isEmpty()) {
            throw new ClientePossuiSimulacoesException("Cliente não pode ser removido pois possui simulações associadas a ele");
        }

        clienteRepository.delete(clienteExistente);
    }

    /**
     * TODO: Pass this to Repository
     * Calcula uma lista com os nomes dos clientes que moram em uma determinada cidade e estado.
     *
     * @param clientes lista dos clientes usados
    como base para o cálculo
     * @param cidade cidade dos clientes a serem
    recuperados
     * @param estado estado da cidade dos clientes
    a serem recuperados
     * @return Lista com os nomes dos clientes
    da lista fornecida que moram na cidade e estado fornecida.
     */
    public List<String> calculaNomesClientesParaCidadeEstado(
        List<Cliente> clientes,
        String cidade,
        String estado
    ) {
        return clientes.stream()
            .filter(cliente -> cliente.getEndereco().getCidade().equals(cidade))
            .filter(cliente -> cliente.getEndereco().getEstado().equals(Estado.valueOf(estado)))
            .map(Cliente::getNome)
            .toList();
    }
}
