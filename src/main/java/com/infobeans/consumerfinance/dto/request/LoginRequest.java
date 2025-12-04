package com.infobeans.consumerfinance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Login Request DTO
 * Contains username and password for authentication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "Username for authentication", example = "admin")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password for authentication", example = "admin123")
    private String password;
}
