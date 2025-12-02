package com.wallet.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String userName;
    private String email;
    private String phoneNo;
    private String address;
    private boolean kycVerified;
    private String role;
}
