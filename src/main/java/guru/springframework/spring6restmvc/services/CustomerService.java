package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.model.CustomerDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerService {

    List<CustomerDto> listCustomers();

    Optional<CustomerDto> getCustomerById(UUID id);

    CustomerDto saveNewCustomer(CustomerDto customer);

    void deleteById(UUID customerId);

    void updateCustomerById(UUID customerId, CustomerDto customer);
}
