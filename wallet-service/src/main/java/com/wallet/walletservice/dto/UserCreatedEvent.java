package com.wallet.walletservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserCreatedEvent {
    private Long id;
    private String userName;
    private String email;
    private String phoneNo;
    private String address;
    private boolean kycVerified;
    private String role;
}
