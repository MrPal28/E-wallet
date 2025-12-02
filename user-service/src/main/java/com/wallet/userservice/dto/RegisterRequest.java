package com.wallet.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "User name cannot be blank")
    @Size(max = 50, message = "User name cannot exceed 50 characters")
    private String userName;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
    private String password;

    @NotBlank(message = "Phone number cannot be blank")
    @Size(min = 10, max = 15, message = "Phone number must be valid")
    private String phoneNo;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;
}
