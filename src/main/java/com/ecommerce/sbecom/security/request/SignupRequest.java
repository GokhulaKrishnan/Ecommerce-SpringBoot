package com.ecommerce.sbecom.security.request;

import com.ecommerce.sbecom.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class SignupRequest {

    @NotBlank
    @Size(min = 3, max = 20)
    String userName;

    @NotBlank
    @Size(min = 6, max = 40)
    String password;

    @Email
    @NotBlank
    String email;

    Set<String> roles;

}
