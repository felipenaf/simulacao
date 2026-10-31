package com.example.simulation.repository;

import com.example.simulation.entity.Customer;
import com.example.simulation.entity.Simulation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulationRepository extends JpaRepository<Simulation, Long> {
    Page<Simulation> findByCustomer(Customer customer, Pageable pageable);
}
