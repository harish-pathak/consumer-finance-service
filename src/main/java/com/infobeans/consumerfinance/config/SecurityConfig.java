package com.infobeans.consumerfinance.config;

import com.infobeans.consumerfinance.security.JwtAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for JWT authentication.
 *
 * PRODUCTION MODE: All API endpoints except health, test, and auth require valid JWT tokens.
 *
 * Security Rules:
 * - Public endpoints (no authentication required):
 *   - POST /api/v1/auth/login (login to get JWT token)
 *   - GET /api/v1/health/** (health checks and readiness probes)
 *   - GET /api/v1/test/** (test/demo endpoints only)
 *   - Swagger UI and OpenAPI docs
 *   - GET /actuator/health (basic health check)
 *
 * - Protected endpoints (require valid JWT Bearer token):
 *   - POST /api/v1/consumers/** (consumer onboarding)
 *   - GET /api/v1/loan-applications/** (view applications)
 *   - POST /api/v1/loan-applications (create applications)
 *   - GET /api/v1/loan-applications/{id}/status (check status)
 *   - POST /api/v1/loan-applications/{id}/decisions (approve/reject - staff only)
 *   - All other API endpoints not explicitly whitelisted
 *
 * Token Validation:
 * All protected endpoint requests MUST include:
 *   Authorization: Bearer <valid_jwt_token>
 *
 * Invalid, missing, or expired tokens return 401 Unauthorized.
 *
 * Staff Authorization:
 * - Decision endpoints require staff authorization (checked at controller/service level)
 * - Extracted from JWT authentication principal
 *
 * @author Consumer Finance Service
 * @version 4.0
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configure HTTP Security filter chain for JWT enforcement.
     *
     * Rules:
     * 1. CSRF disabled for stateless REST API (JWT tokens used instead)
     * 2. Session management set to STATELESS (no session cookies)
     * 3. Only auth, health, and test endpoints are public
     * 4. All consumer API endpoints require valid JWT authentication
     * 5. JWT filter added before UsernamePasswordAuthenticationFilter
     *
     * @param http HttpSecurity object
     * @return SecurityFilterChain configured for JWT
     * @throws Exception if security configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // Disable CSRF for stateless REST API (JWT-based)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // ===== PUBLIC ENDPOINTS (NO AUTHENTICATION) =====

                // Authentication endpoint - required to get JWT token
                .requestMatchers("/api/v1/auth/**").permitAll()

                // Health check endpoints - required for Kubernetes/Docker health probes
                .requestMatchers("/api/v1/health/**").permitAll()

                // Test/demo endpoints - for development/testing only
                // Note: These should be disabled or restricted in production
                .requestMatchers("/api/v1/test/**").permitAll()

                // Swagger/OpenAPI documentation - optional (can be restricted in production)
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                // Basic actuator health - for monitoring/load balancers
                .requestMatchers("/actuator/health").permitAll()

                // ===== PROTECTED ENDPOINTS (JWT REQUIRED) =====

                // Consumer endpoints - REQUIRES JWT TOKEN
                .requestMatchers("/api/v1/consumers/**").authenticated()

                // Loan Application endpoints - REQUIRES JWT TOKEN
                .requestMatchers("/api/v1/loan-applications/**").authenticated()

                // Principal Account endpoints - REQUIRES JWT TOKEN
                .requestMatchers("/api/v1/principal-accounts/**").authenticated()
                .requestMatchers("/api/v1/*/principal-account").authenticated()

                // Vendor Linked Account endpoints - REQUIRES JWT TOKEN
                .requestMatchers("/api/v1/vendor-accounts/**").authenticated()
                .requestMatchers("/api/v1/*/vendor-accounts/**").authenticated()

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            // Add JWT filter before default authentication filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("✅ PRODUCTION MODE: JWT authentication enabled for all protected endpoints");
        log.info("Step 1: POST /api/v1/auth/login with credentials to get JWT token");
        log.info("Step 2: Use token in Authorization header: Authorization: Bearer <JWT_TOKEN>");
        log.info("Step 3: Access protected endpoints like /api/v1/consumers/**");

        return http.build();
    }
}
