package com.infobeans.consumerfinance.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

/**
 * Spring Security configuration for OAuth2 Authentication (Auth0).
 *
 * PRODUCTION MODE: All API endpoints except health, test endpoints require valid OAuth2 JWT tokens.
 *
 * OAuth2 Provider: Auth0 (https://auth0.com)
 * - Centralized user management and authentication
 * - Industry-standard OAuth 2.0 / OpenID Connect (OIDC)
 * - Automatic token validation using JWK Set from Auth0
 *
 * Security Rules:
 * - Public endpoints (no authentication required):
 *   - GET /api/v1/health/** (health checks and readiness probes)
 *   - GET /api/v1/test/** (test/demo endpoints only)
 *   - Swagger UI and OpenAPI docs
 *   - GET /actuator/health (basic health check)
 *
 * - Protected endpoints (require valid OAuth2 JWT Bearer token from Auth0):
 *   - POST /api/v1/consumers/** (consumer onboarding)
 *   - GET /api/v1/loan-applications/** (view applications)
 *   - POST /api/v1/loan-applications (create applications)
 *   - GET /api/v1/loan-applications/{id}/status (check status)
 *   - POST /api/v1/loan-applications/{id}/decisions (approve/reject - staff only)
 *   - All other API endpoints not explicitly whitelisted
 *
 * Token Validation:
 * All protected endpoint requests MUST include:
 *   Authorization: Bearer <auth0_access_token>
 *
 * Token is validated automatically by Spring Security OAuth2 Resource Server:
 * - Signature verification using Auth0 JWK Set
 * - Issuer verification (must match configured Auth0 domain)
 * - Expiration check
 * - Audience verification (optional, configure if needed)
 *
 * Invalid, missing, or expired tokens return 401 Unauthorized.
 *
 * Getting Tokens:
 * 1. Authenticate with Auth0 (via OAuth2 flow or API)
 * 2. Obtain access_token from Auth0 token endpoint
 * 3. Include token in Authorization header: Bearer <token>
 *
 * Staff Authorization:
 * - Decision endpoints require staff authorization (checked at controller/service level)
 * - Staff roles/claims extracted from Auth0 JWT token
 * - Configure roles in Auth0 dashboard and include in token claims
 *
 * Configuration:
 * - Auth0 domain configured in application.yml:
 *   spring.security.oauth2.resourceserver.jwt.issuer-uri
 *
 * @author Consumer Finance Service
 * @version 5.0 (OAuth2 Migration)
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String oauth2IssuerUri;

    /**
     * Configure HTTP Security filter chain for OAuth2 JWT enforcement.
     *
     * Rules:
     * 1. CSRF disabled for stateless REST API (OAuth2 JWT tokens used instead)
     * 2. Session management set to STATELESS (no session cookies)
     * 3. Only health and test endpoints are public
     * 4. All API endpoints require valid OAuth2 JWT authentication from Auth0
     * 5. OAuth2 Resource Server configured for automatic JWT validation
     *
     * OAuth2 Resource Server:
     * - Automatically validates JWT tokens from Auth0
     * - Verifies signature using JWK Set from Auth0
     * - Checks issuer, expiration, and other claims
     * - No custom filter needed - handled by Spring Security
     *
     * @param http HttpSecurity object
     * @return SecurityFilterChain configured for OAuth2 JWT
     * @throws Exception if security configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // Disable CSRF for stateless REST API (OAuth2 JWT-based)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // ===== PUBLIC ENDPOINTS (NO AUTHENTICATION) =====

                // Health check endpoints - required for Kubernetes/Docker health probes
                .requestMatchers("/api/v1/health/**").permitAll()

                // Test/demo endpoints - for development/testing only
                // Note: These should be disabled or restricted in production
                .requestMatchers("/api/v1/test/**").permitAll()

                // Swagger/OpenAPI documentation - optional (can be restricted in production)
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                // Basic actuator health - for monitoring/load balancers
                .requestMatchers("/actuator/health").permitAll()

                // ===== PROTECTED ENDPOINTS (OAUTH2 JWT REQUIRED) =====

                // Consumer endpoints - REQUIRES AUTH0 JWT TOKEN
                .requestMatchers("/api/v1/consumers/**").authenticated()

                // Loan Application endpoints - REQUIRES AUTH0 JWT TOKEN
                .requestMatchers("/api/v1/loan-applications/**").authenticated()

                // Principal Account endpoints - REQUIRES AUTH0 JWT TOKEN
                .requestMatchers("/api/v1/principal-accounts/**").authenticated()
                .requestMatchers("/api/v1/*/principal-account").authenticated()

                // Vendor Linked Account endpoints - REQUIRES AUTH0 JWT TOKEN
                .requestMatchers("/api/v1/vendor-accounts/**").authenticated()
                .requestMatchers("/api/v1/*/vendor-accounts/**").authenticated()

                // All other endpoints require authentication
                .anyRequest().authenticated()
            );

        // Configure OAuth2 Resource Server for JWT validation ONLY if issuer-uri is configured
        if (StringUtils.hasText(oauth2IssuerUri)) {
            http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {
                    // JWT decoder auto-configured from application.yml issuer-uri
                    // Validates:
                    // - Signature using Auth0 JWK Set
                    // - Issuer matches Auth0 domain
                    // - Token not expired
                    // - Standard JWT claims
                    log.debug("OAuth2 JWT Resource Server configured for Auth0");
                })
            );

            log.info("✅ PRODUCTION MODE: OAuth2 authentication enabled with Auth0");
            log.info("Step 1: Authenticate with Auth0 to get access_token");
            log.info("Step 2: Use token in Authorization header: Authorization: Bearer <AUTH0_TOKEN>");
            log.info("Step 3: Access protected endpoints like /api/v1/consumers/**");
            log.info("Token validation: Automatic signature verification via Auth0 JWK Set");
        } else {
            log.warn("⚠️  TEST MODE: OAuth2 disabled - issuer-uri not configured");
            log.warn("All endpoints will be accessible without authentication");
        }

        return http.build();
    }
}
