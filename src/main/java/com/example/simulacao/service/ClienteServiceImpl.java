package com.example.simulacao.service;

import com.example.simulacao.dto.ClientePatchRequest;
import com.example.simulacao.dto.ClienteRequest;
import com.example.simulacao.dto.ClienteResponse;
import com.example.simulacao.entity.Cliente;
import com.example.simulacao.entity.Endereco;
import com.example.simulacao.exception.ClienteNaoEncontradoException;
import com.example.simulacao.exception.CpfJaCadastradoException;
import com.example.simulacao.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ClienteServiceImpl implements ClienteService {
    private final ClienteRepository clienteRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    public ClienteResponse save(ClienteRequest request) {
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

        return toResponse(cliente);
    }

    @Override
    public ClienteResponse findById(Long id) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() ->
                new ClienteNaoEncontradoException("Cliente não encontrado: " + id)
            );

        return toResponse(cliente);
    }

    @Override
    public Page<ClienteResponse> findAll(Pageable pageable) {
       return clienteRepository
           .findAll(pageable)
           .map(this::toResponse);
    }

    @Override
    @Transactional
    public ClienteResponse patch(Long id, ClientePatchRequest request) {
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

        return toResponse(clienteExistente);
    }

    @Override
    @Transactional
    public ClienteResponse put(Long id, ClienteRequest request) {
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

        return toResponse(clienteExistente);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Cliente clienteExistente = clienteRepository.findById(id)
            .orElseThrow(() ->
                new ClienteNaoEncontradoException("Cliente não encontrado: " + id)
            );

        //TODO: Consultar se o cliente possui simulações associadas a ele

        clienteRepository.delete(clienteExistente);
    }

    private ClienteResponse toResponse(Cliente cliente) {
        ClienteResponse.EnderecoResponse enderecoResponse =
            new ClienteResponse.EnderecoResponse(
                cliente.getEndereco().getRua(),
                cliente.getEndereco().getNumero(),
                cliente.getEndereco().getBairro(),
                cliente.getEndereco().getCep(),
                cliente.getEndereco().getCidade(),
                cliente.getEndereco().getEstado()
            );

        return new ClienteResponse(
            cliente.getCpf(),
            cliente.getNome(),
            enderecoResponse
        );
    }
}
