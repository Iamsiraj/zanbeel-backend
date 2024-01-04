package com.iconsult.userservice.controller;

import com.iconsult.userservice.model.dto.request.CustomerDto;
import com.iconsult.userservice.model.dto.request.ForgetUsernameDto;
import com.iconsult.userservice.model.dto.request.OTPDto;
import com.iconsult.userservice.model.dto.request.LoginDto;
import com.iconsult.userservice.model.dto.response.ResponseDTO;
import com.iconsult.userservice.model.entity.Customer;
import com.iconsult.userservice.service.Impl.CustomerServiceImpl;
import com.iconsult.userservice.service.Impl.OTPLogImpl;
import com.zanbeel.customUtility.model.CustomResponseEntity;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/v1/customer")
public class CustomerController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerServiceImpl customerServiceImpl;

    @Autowired
    private OTPLogImpl otpLogImpl;

    @GetMapping("/ping")
    public String ping()
    {
        LOGGER.info("User-Service is running {}", LocalDateTime.now());
        return "Hello World";
    }

    @PostMapping("/signup")
    public CustomResponseEntity<ResponseDTO> register(@Valid @RequestBody CustomerDto customerDto)
    {
        return this.customerServiceImpl.register(customerDto);
    }

    @PostMapping("/createOTP")
    public CustomResponseEntity<ResponseDTO> createOTP(@Valid @RequestBody OTPDto OTPDto)
    {
        return this.otpLogImpl.createOTP(OTPDto);
    }

    @PostMapping("/verifyOTP")
    public CustomResponseEntity<ResponseDTO> verifyOTP(@Valid @RequestBody OTPDto OTPDto)
    {
        return this.otpLogImpl.verifyOTP(OTPDto);
    }

    @GetMapping("/verifyCNIC")
    public CustomResponseEntity<Boolean> verifyCNIC(@RequestParam String cnic, @RequestParam String accountNumber)
    {
        return this.customerServiceImpl.verifyCNIC(cnic, accountNumber);
    }

    @PostMapping("/login")
    public CustomResponseEntity<ResponseDTO> login(@Valid @RequestBody LoginDto loginDto)
    {
        return this.customerServiceImpl.login(loginDto);
    }

    @PostMapping("/forgetUserName")
    public CustomResponseEntity<ResponseDTO> forgotUserName(@Valid @RequestBody ForgetUsernameDto forgetUsernameDto)
    {
        return this.customerServiceImpl.forgetUserName(forgetUsernameDto);
    }

    @GetMapping("/{id}")
    public CustomResponseEntity<Customer> findById(@PathVariable Long id)
    {
        return this.customerServiceImpl.findById(id);
    }
}