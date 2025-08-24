package com.example.dto;

import lombok.Data;

@Data
public class KycRequest {
    private String panNumber;
    private String aadhaarNumber;
    private String address;
}
