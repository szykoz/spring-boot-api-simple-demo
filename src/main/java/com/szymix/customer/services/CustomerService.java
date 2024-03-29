package com.szymix.customer.services;

import com.sun.jdi.request.DuplicateRequestException;
import com.szymix.customer.dao.CustomerDao;
import com.szymix.customer.dtos.CustomerRegistrationRequest;
import com.szymix.customer.dtos.CustomerUpdateRequest;
import com.szymix.customer.models.Customer;
import com.szymix.exceptions.RequestValidationException;
import com.szymix.exceptions.ResourceDuplicateException;
import com.szymix.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.szymix.customer.mappers.CustomerMapper.mapRegistrationRequestToCustomer;

@Service
public class CustomerService {

    private final CustomerDao customerDao;

    public CustomerService(@Qualifier("jdbc") CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    public List<Customer> getAllCustomers() {
        return customerDao.selectAllCustomers();
    }

    public Customer getCustomer(Long id) {
        return customerDao.selectCustomerById(id)
                .orElseThrow(() -> new ResourceNotFoundException("customer with id [%s] not found".formatted(id)));
    }

    public void addCustomer(CustomerRegistrationRequest customerRegistrationRequest) {

        String email = customerRegistrationRequest.email();
        if (customerDao.existsCustomerWithEmail(email)) {
            throw new ResourceDuplicateException("email already taken");
        }
        Customer newCustomer = new Customer();
        mapRegistrationRequestToCustomer(customerRegistrationRequest, newCustomer);
        customerDao.insertCustomer(newCustomer);
    }

    public void deleteCustomerById(Long id) {
        if (!customerDao.existsCustomerWithId(id)) {
            throw new ResourceNotFoundException("customer with id [%s] not found".formatted(id));
        }
        customerDao.deleteCustomerById(id);
    }

    public void updateCustomer(Long id, CustomerUpdateRequest updateRequest) {

        Customer customer = getCustomer(id);
        boolean changes = false;

        if (updateRequest.name() != null && !updateRequest.name().equals(customer.getName())) {
            customer.setName(updateRequest.name());
            changes = true;
        }
        if (updateRequest.age() != null && !updateRequest.age().equals(customer.getAge())) {
            customer.setAge(updateRequest.age());
            changes = true;
        }
        if (updateRequest.email() != null && !updateRequest.email().equals(customer.getEmail())) {
            if (customerDao.existsCustomerWithEmail(updateRequest.email())) {
                throw new DuplicateRequestException("email already taken");
            }
            customer.setEmail(updateRequest.email());
            changes = true;
        }
        if (!changes) {
            throw new RequestValidationException("no changes found");
        }
        customerDao.updateCustomer(customer);
    }
}
