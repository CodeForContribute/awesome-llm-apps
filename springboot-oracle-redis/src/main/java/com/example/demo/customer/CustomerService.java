package com.example.demo.customer;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public List<Customer> findAll() {
        return repository.findAll();
    }

    @Cacheable(value = "customers", key = "#id")
    public Customer findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @Transactional
    @CachePut(value = "customers", key = "#result.id")
    public Customer create(CustomerRequest request) {
        repository.findByEmail(request.getEmail()).ifPresent(existing -> {
            throw new DuplicateEmailException(existing.getEmail());
        });
        Customer customer = new Customer(request.getName(), request.getEmail());
        return repository.save(customer);
    }

    @Transactional
    @CachePut(value = "customers", key = "#id")
    public Customer update(Long id, CustomerRequest request) {
        Customer existing = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        if (!existing.getEmail().equalsIgnoreCase(request.getEmail())) {
            repository.findByEmail(request.getEmail()).ifPresent(other -> {
                throw new DuplicateEmailException(other.getEmail());
            });
        }
        existing.setName(request.getName());
        existing.setEmail(request.getEmail());
        return repository.save(existing);
    }

    @Transactional
    @CacheEvict(value = "customers", key = "#id")
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
