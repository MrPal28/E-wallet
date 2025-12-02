package com.wallet.userservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_phone", columnList = "phone_no")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_email", columnNames = "email")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    // -------------------- User Name --------------------
    @NotBlank(message = "User name cannot be blank")
    @Size(max = 50, message = "Name cannot exceed 50 characters")
    @Column(name = "name", nullable = false)
    private String name;

    // -------------------- Password ---------------------
    @JsonIgnore
    @NotBlank(message = "Password cannot be blank")
    @Column(nullable = false)
    private String password;

    // -------------------- Phone Number -----------------
    @Size(min = 10, max = 15, message = "Phone number must be valid")
    @Column(name = "phone_no")
    private String phoneNo;

    // -------------------- Address ----------------------
    @Size(max = 255)
    private String address;

    // -------------------- Email ------------------------
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be blank")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    // -------------------- KYC / Account Flags ----------
    @Builder.Default
    @Column(name = "kyc_verified")
    private boolean kycVerified = false;

    @Builder.Default
    @Column(name = "active")
    private boolean active = true;

    @Builder.Default
    @Column(name = "role", nullable = false)
    private String role = "USER";
}
