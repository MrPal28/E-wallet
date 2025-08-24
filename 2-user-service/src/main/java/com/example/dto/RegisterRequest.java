package com.example.dto;


import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
  @NotBlank(message = "userName cant blank/null")
	@Column(unique = true)
	private String userName;
	@NotBlank(message = "password cant blank/null")
	private String password;
	@Min(value = 1000, message = "phone number min val not sufficient")
	private String phoneNo;
	private String address;
	private String email;
}
