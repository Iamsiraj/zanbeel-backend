package com.iconsult.userservice.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDto {

    @NotEmpty(message = "Email is mandatory")
    @Email
    private String email;
    @NotEmpty(message = "Password is mandatory")
    private String password;
}
