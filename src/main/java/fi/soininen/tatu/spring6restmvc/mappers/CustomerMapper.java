package fi.soininen.tatu.spring6restmvc.mappers;

import fi.soininen.tatu.spring6restmvc.entities.Customer;
import fi.soininen.tatu.spring6restmvc.model.CustomerDto;
import org.mapstruct.Mapper;

@Mapper
public interface CustomerMapper {
    Customer customerDtoToCustomer(CustomerDto dto);
    CustomerDto customerToCustomerDto(Customer customer);
}
