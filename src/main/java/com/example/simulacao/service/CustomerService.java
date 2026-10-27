package com.example.simulacao.service;

import com.example.simulacao.dto.CustomerPatchRequest;
import com.example.simulacao.dto.CustomerRequest;
import com.example.simulacao.dto.CustomerResponse;
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
