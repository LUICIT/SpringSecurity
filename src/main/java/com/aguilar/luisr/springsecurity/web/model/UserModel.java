package com.aguilar.luisr.springsecurity.web.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class UserModel {

    private Long id;

    @NotBlank
    @Size(min = 1, max = 90)
    private String names;

    @NotBlank
    @Size(min = 1, max = 90)
    private String lastName;

    @Size(min = 1, max = 90)
    private String secondLastName;

    @NotBlank
    @Email
    @Size(min = 5, max = 120)
    private String email;

    @Size(min = 1, max = 10)
    private String phone;

    @NotBlank
    @Size(min = 1, max = 50)
    private String userType;

}
