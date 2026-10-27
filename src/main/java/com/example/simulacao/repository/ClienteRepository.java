package com.example.simulacao.repository;

import com.example.simulacao.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCpf(String cpf);
}
