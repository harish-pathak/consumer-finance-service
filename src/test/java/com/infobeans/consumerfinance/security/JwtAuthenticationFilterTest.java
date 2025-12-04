package com.infobeans.consumerfinance.security;

import com.infobeans.consumerfinance.util.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationFilter
 * Tests JWT token extraction and authentication context setup
 */
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should extract valid JWT token from Authorization header")
    void testDoFilterInternal_ValidToken() throws Exception {
        // Arrange
        String token = "valid.jwt.token";
        String username = "testuser";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn(username);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(username, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not set authentication when Authorization header is missing")
    void testDoFilterInternal_MissingAuthHeader() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(any());
    }

    @Test
    @DisplayName("Should not set authentication when Authorization header is empty")
    void testDoFilterInternal_EmptyAuthHeader() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtTokenProvider, never()).validateToken(any());
    }

    @Test
    @DisplayName("Should not set authentication when Bearer prefix is missing")
    void testDoFilterInternal_NoBearerPrefix() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("BasicAuth token123");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtTokenProvider, never()).validateToken(any());
    }

    @Test
    @DisplayName("Should continue filter chain even with invalid token")
    void testDoFilterInternal_InvalidToken() throws Exception {
        // Arrange
        String token = "invalid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not extract username from invalid token")
    void testDoFilterInternal_NoUsernameExtraction_ForInvalidToken() throws Exception {
        // Arrange
        String token = "invalid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtTokenProvider).validateToken(token);
        verify(jwtTokenProvider, never()).getUsernameFromToken(any());
    }

    @Test
    @DisplayName("Should handle exception gracefully and continue filter chain")
    void testDoFilterInternal_Exception() throws Exception {
        // Arrange
        String token = "valid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenThrow(new RuntimeException("Token validation error"));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should extract token correctly when username is null")
    void testDoFilterInternal_NullUsername() throws Exception {
        // Arrange
        String token = "valid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should handle Bearer token with extra whitespace")
    void testDoFilterInternal_BearerWithExtraWhitespace() throws Exception {
        // Arrange
        String token = "valid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer  " + token); // Extra space after Bearer
        when(jwtTokenProvider.validateToken(any())).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert - Should not validate due to extra space
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should always call filterChain.doFilter regardless of token validity")
    void testDoFilterInternal_AlwaysCallsFilterChain() throws Exception {
        // Arrange - valid token
        String validToken = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(validToken)).thenReturn("user1");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);

        // Reset and test with invalid token
        SecurityContextHolder.clearContext();

        String invalidToken = "invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(2)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should set correct authentication type")
    void testDoFilterInternal_AuthenticationType() throws Exception {
        // Arrange
        String token = "valid.jwt.token";
        String username = "testuser@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn(username);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("org.springframework.security.authentication.UsernamePasswordAuthenticationToken",
                SecurityContextHolder.getContext().getAuthentication().getClass().getCanonicalName());
    }
}
