package com.aguilar.luisr.springsecurity.web.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginModel {

    @NotBlank
    @Email
    @Size(min = 5, max = 120)
    private String email;

    @NotBlank
    private String password;

}
