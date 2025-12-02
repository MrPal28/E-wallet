package com.wallet.userservice.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KycStatusResponse {
    private boolean kycVerified;
    private String status;
    private LocalDateTime lastUpdated;
}
