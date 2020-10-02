package com.santosh.ShopingCart.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class LoginDto {

    @NotNull
    @Email
    private String email;
    @NotNull
    private String password;


}
