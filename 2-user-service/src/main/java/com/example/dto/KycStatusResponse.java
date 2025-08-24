package com.example.dto;

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
    private String status; // e.g., "PENDING", "VERIFIED", "REJECTED"
    private LocalDateTime lastUpdated;
}
