package com.wallet.userservice.dto;

import lombok.Data;

@Data
public class KycRequest {
    private String panNumber;
    private String aadhaarNumber;
    private String address;
}
