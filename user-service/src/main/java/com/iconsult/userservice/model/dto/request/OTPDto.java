package com.iconsult.userservice.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OTPDto {

    @NotEmpty(message = "Mobile Number is mandatory")
    @Pattern(regexp = "^033\\d{8}$", message = "Invalid Mobile number")
    private String mobileNumber;

    @Email
    private String email;

    private String otp;
    private String reason;

    public OTPDto(String mobileNumber, String email) {
        this.mobileNumber = mobileNumber;
        this.email = email;
    }
}
