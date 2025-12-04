package com.infobeans.consumerfinance.controller;

import com.infobeans.consumerfinance.dto.request.LoginRequest;
import com.infobeans.consumerfinance.dto.response.LoginResponse;
import com.infobeans.consumerfinance.util.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication Controller
 * Handles JWT token generation for authorized users.
 *
 * Public Endpoint: POST /api/v1/auth/login
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "JWT Authentication endpoints")
@Slf4j
public class AuthController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${spring.security.user.username}")
    private String appUsername;

    @Value("${spring.security.user.password}")
    private String appPassword;

    /**
     * Generate JWT token for authenticated user
     *
     * Public endpoint - no authorization required
     *
     * @param loginRequest containing username and password
     * @return JWT token if credentials are valid, 401 if invalid
     */
    @PostMapping("/login")
    @Operation(summary = "Generate JWT Token",
               description = "Authenticate user and generate JWT token for accessing protected endpoints")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Validate credentials against configured user
            if (!appUsername.equals(loginRequest.getUsername()) ||
                !appPassword.equals(loginRequest.getPassword())) {
                log.warn("Login failed - Invalid credentials for username: {}", loginRequest.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(LoginResponse.builder()
                                .success(false)
                                .message("Invalid username or password")
                                .build());
            }

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(loginRequest.getUsername());

            log.info("JWT token generated successfully for user: {}", loginRequest.getUsername());
            return ResponseEntity.ok(LoginResponse.builder()
                    .success(true)
                    .message("Login successful")
                    .token(token)
                    .username(loginRequest.getUsername())
                    .build());

        } catch (Exception e) {
            log.error("Error during login: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LoginResponse.builder()
                            .success(false)
                            .message("An error occurred during login")
                            .build());
        }
    }
}
