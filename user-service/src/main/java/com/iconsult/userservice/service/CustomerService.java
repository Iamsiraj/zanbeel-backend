package com.iconsult.userservice.service;



import com.iconsult.userservice.model.dto.request.CustomerDto;
import com.iconsult.userservice.model.dto.request.LoginDto;
import com.iconsult.userservice.model.dto.request.OTPDto;
import com.iconsult.userservice.model.dto.response.ResponseDTO;
import com.iconsult.userservice.model.entity.Customer;
import com.zanbeel.customUtility.model.CustomResponseEntity;

public interface CustomerService
{
    Customer addUser(Customer customer);

    CustomResponseEntity<ResponseDTO> register(CustomerDto customerDto);

    CustomResponseEntity<ResponseDTO> login(LoginDto loginDto);

    void deleteUser(Long id);

    Customer updateCustomer(Customer customer);

    Customer findByEmailAddress(String email);

    Customer findByMobileNumber(String mobileNumber);

    Customer save(Customer customer);

    CustomResponseEntity<Customer> findById(Long id);
}
