package com.example.simulacao.repository;

import com.example.simulacao.entity.Cliente;
import com.example.simulacao.entity.Simulacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulacaoRepository extends JpaRepository<Simulacao, Long> {
    Page<Simulacao> findByCliente(Cliente cliente, Pageable pageable);
}
