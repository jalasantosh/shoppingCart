package com.santosh.ShopingCart.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDto {
    private String email;
    private String oldPassword;
    private String newPassword;
}
