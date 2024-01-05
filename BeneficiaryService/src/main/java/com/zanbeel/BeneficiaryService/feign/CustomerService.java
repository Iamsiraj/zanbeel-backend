package com.zanbeel.BeneficiaryService.feign;

import com.zanbeel.BeneficiaryService.model.dto.response.CustomerResponseDto;
import com.zanbeel.customUtility.model.CustomResponseEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient("USER-SERVICE")
public interface CustomerService {

    @GetMapping(value = "/v1/customer/{id}")
    CustomResponseEntity<CustomerResponseDto> getCustomerById(@PathVariable Long id);
}