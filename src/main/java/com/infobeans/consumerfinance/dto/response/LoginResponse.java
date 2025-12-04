package com.infobeans.consumerfinance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Login Response DTO
 * Contains JWT token and authentication status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    @Schema(description = "Authentication success status", example = "true")
    private Boolean success;

    @Schema(description = "Response message", example = "Login successful")
    private String message;

    @Schema(description = "JWT Token for authenticated requests", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Authenticated username", example = "admin")
    private String username;
}
