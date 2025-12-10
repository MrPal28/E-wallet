package com.wallet.walletservice.entity;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.CurrentTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "wallets")
@AllArgsConstructor
@NoArgsConstructor
public class Wallet {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int walletId;
	@Min(value = 50, message = "Wallet balance must be at least ₹500")
	private BigDecimal walletBalance;

	@CurrentTimestamp
	private LocalDate createdDate;
	 @UpdateTimestamp
   private LocalDate lastUpdated;

	@Enumerated(value = EnumType.STRING)
	private WalletStatus status;
	@Column(unique = true)
	private Long userId;

}
