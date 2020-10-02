package com.santosh.ShopingCart.entites;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotNull
    private String firstName;
    private String lastName;
    @Column(unique = true)
    @Email
    @NotNull
    private String email;
    @NotNull
    private String mobile;
    private Status status;

    private String otp;
    private Date createdOtpTime;
    private Date expiryOtpTime;

    // @JsonIgnore
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

}
