package com.example.model;

import com.example.validation.Date;
import com.example.validation.NhsNumber;
import com.example.validation.Postcode;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record Patient(@NhsNumber @NotNull String nhsNumber, @NotBlank @Size(max = 100) String name, @Date @NotNull String dateOfBirth, @Size(max = 100) String address,
            @Postcode String postcode, @Size(max = 20) String telephoneNumber, @Email String emailAddress) {
}
