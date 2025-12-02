package com.wallet.userservice.entity;

import java.time.LocalDateTime;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kyc {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long kycId;

  private Long userId; // reference to user entity

  @Column(unique = true)
  @NotNull(message = "PAN NUMBER CANNOT BE NULL")
  private String panNumber;
  @Column(unique = true)
  @NotNull(message = "AADHAR NUMBER CANNOT BE NULL")
  private String aadharNumber;
  private String address;
  private boolean kycVerified;
  private String status;
  private LocalDateTime lastUpdatedAt;
}
