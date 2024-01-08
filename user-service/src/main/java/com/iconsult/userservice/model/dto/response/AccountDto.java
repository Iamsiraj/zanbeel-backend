package com.iconsult.userservice.model.dto.response;

import lombok.Data;

@Data
public class AccountDto
{
    private String name;

    private String cnic;

    private String dob;

    private String accountNumber;

    private String accountType;

    private String branch;

    private String bankName;

    private String city;

    private String email;

    private String cellNumber;

    private String cnicIssuance;

    private String cnicExpiry;

    private String purposeOfAccount;

    private String sourceOfIncome;

    private String residentialAddress;

    private String lineOfBusiness;

    private String businessAddress;

    private Long customerId;
}
