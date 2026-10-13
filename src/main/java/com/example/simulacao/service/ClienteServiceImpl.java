package com.example.simulacao.service;

import com.example.simulacao.dto.ClienteRequest;
import com.example.simulacao.entity.Cliente;
import com.example.simulacao.entity.Endereco;
import com.example.simulacao.exception.CpfJaCadastradoException;
import com.example.simulacao.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
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
    public void save(ClienteRequest request) {
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
    }
}
