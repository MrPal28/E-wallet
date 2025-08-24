package com.example.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;
	@NotNull(message = "userName cant blank/null")
	@NotBlank(message = "userName cant blank/null")
	@Column(unique = true)
	private String name;
	@NotNull(message = "password cant blank/null")
	@NotBlank(message = "password cant blank/null")
	// @Length (value =8,message = "password have atleast 8 chars")
	@JsonIgnore
	private String password;
	@Min(value = 1000, message = "phone number min val not sufficient")
	private String phoneNo;
	private String address;
	private String email;
	@Builder.Default
	private boolean kycVerified = false;
	

}
//username=null
//username=""