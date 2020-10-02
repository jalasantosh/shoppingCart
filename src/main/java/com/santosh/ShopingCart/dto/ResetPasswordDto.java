package com.santosh.ShopingCart.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ResetPasswordDto {
    @NotNull
    @Email
    private String email;
    @NotNull
    private String otp;
    @NotNull
    private String password;
}
