package com.iconsult.userservice.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OTPDto {

    @NotEmpty(message = "Mobile Number is mandatory")
    @Pattern(regexp = "^033\\d{8}$", message = "Invalid Mobile number")
    private String mobileNumber;

    @Email
    private String email;

    private String otp;
}
