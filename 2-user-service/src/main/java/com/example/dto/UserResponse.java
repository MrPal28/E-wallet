package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String userName;
    private String email;
    private String phoneNo;
    private String address;
    private boolean kycVerified;
    private String role;
}
