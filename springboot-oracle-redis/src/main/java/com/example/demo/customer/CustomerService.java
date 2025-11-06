package com.example.demo.customer;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@CacheConfig(cacheNames = "customers")
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public List<Customer> findAll() {
        return repository.findAll();
    }

    @Cacheable(key = "#id")
    public Customer findById(Long id) {
        return requireCustomer(id);
    }

    @Transactional
    @CachePut(key = "#result.id")
    public Customer create(CustomerRequest request) {
        ensureEmailIsUnique(request.email(), null);
        Customer customer = new Customer(request.name(), request.email());
        return repository.save(customer);
    }

    @Transactional
    @CachePut(key = "#id")
    public Customer update(Long id, CustomerRequest request) {
        Customer existing = requireCustomer(id);
        if (!existing.getEmail().equalsIgnoreCase(request.email())) {
            ensureEmailIsUnique(request.email(), id);
        }
        existing.setName(request.name());
        existing.setEmail(request.email());
        return repository.save(existing);
    }

    @Transactional
    @CacheEvict(key = "#id")
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private Customer requireCustomer(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    private void ensureEmailIsUnique(String email, Long currentId) {
        repository.findByEmail(email).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new DuplicateEmailException(email);
            }
        });
    }
}
