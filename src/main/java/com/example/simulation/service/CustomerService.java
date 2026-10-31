package com.example.simulation.service;

import com.example.simulation.dto.CustomerPatchRequest;
import com.example.simulation.dto.CustomerRequest;
import com.example.simulation.dto.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    CustomerResponse save(CustomerRequest request);

    CustomerResponse findById(Long id);

    Page<CustomerResponse> findAll(Pageable pageable);

    CustomerResponse patch(Long id, CustomerPatchRequest request);

    CustomerResponse put(Long id, CustomerRequest request);

    void delete(Long id);
}
