package com.infobeans.consumerfinance.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtTokenProvider
 * Tests JWT token generation, validation, and username extraction
 */
@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String TEST_SECRET = "this-is-a-secret-key-that-must-be-at-least-256-bits-long-for-hs256-algorithm";
    private static final long TEST_EXPIRATION_MS = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", TEST_EXPIRATION_MS);
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void testGenerateToken_Success() {
        // Arrange
        String username = "testuser";

        // Act
        String token = jwtTokenProvider.generateToken(username);

        // Assert
        assertNotNull(token);
        assertNotEmpty(token);
        assertTrue(token.contains("."), "Token should contain dots (JWT format)");

        // Verify it has 3 parts (header.payload.signature)
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts");
    }

    @Test
    @DisplayName("Should generate valid tokens multiple times")
    void testGenerateToken_MultipleGenerations() throws InterruptedException {
        // Arrange
        String username = "testuser";

        // Act
        String token1 = jwtTokenProvider.generateToken(username);
        Thread.sleep(10); // Wait to ensure different timestamps
        String token2 = jwtTokenProvider.generateToken(username);

        // Assert - Both tokens should be valid
        assertTrue(jwtTokenProvider.validateToken(token1));
        assertTrue(jwtTokenProvider.validateToken(token2));
        assertEquals(username, jwtTokenProvider.getUsernameFromToken(token1));
        assertEquals(username, jwtTokenProvider.getUsernameFromToken(token2));
    }

    @Test
    @DisplayName("Should extract correct username from token")
    void testGetUsernameFromToken_Success() {
        // Arrange
        String username = "john.doe@example.com";
        String token = jwtTokenProvider.generateToken(username);

        // Act
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertNotNull(extractedUsername);
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Should validate legitimate token")
    void testValidateToken_ValidToken() {
        // Arrange
        String username = "testuser";
        String token = jwtTokenProvider.generateToken(username);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid, "Legitimate token should be valid");
    }

    @Test
    @DisplayName("Should reject malformed token")
    void testValidateToken_MalformedToken() {
        // Arrange
        String malformedToken = "invalid.token.format";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Assert
        assertFalse(isValid, "Malformed token should be invalid");
    }

    @Test
    @DisplayName("Should reject token with invalid signature")
    void testValidateToken_InvalidSignature() {
        // Arrange
        String username = "testuser";
        String validToken = jwtTokenProvider.generateToken(username);

        // Tamper with signature
        String[] parts = validToken.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".invalidsignature";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(tamperedToken);

        // Assert
        assertFalse(isValid, "Token with invalid signature should be invalid");
    }

    @Test
    @DisplayName("Should return null username for invalid token")
    void testGetUsernameFromToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        String username = jwtTokenProvider.getUsernameFromToken(invalidToken);

        // Assert
        assertNull(username, "Should return null for invalid token");
    }

    @Test
    @DisplayName("Should handle special characters in username")
    void testGenerateToken_SpecialCharacters() {
        // Arrange
        String username = "user+special@example.com";

        // Act
        String token = jwtTokenProvider.generateToken(username);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertNotNull(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Should handle long username")
    void testGenerateToken_LongUsername() {
        // Arrange
        String username = "very.long.username.with.many.characters@subdomain.example.com";

        // Act
        String token = jwtTokenProvider.generateToken(username);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Should reject empty token string")
    void testValidateToken_EmptyToken() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject null token")
    void testValidateToken_NullToken() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should generate token with correct structure")
    void testGenerateToken_TokenStructure() {
        // Arrange
        String username = "test@example.com";

        // Act
        String token = jwtTokenProvider.generateToken(username);

        // Assert
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);

        // Each part should be non-empty
        for (String part : parts) {
            assertFalse(part.isEmpty(), "Token parts should not be empty");
        }
    }

    @Test
    @DisplayName("Extracted username should match original username exactly")
    void testGetUsernameFromToken_ExactMatch() {
        // Arrange
        String username = "admin123";
        String token = jwtTokenProvider.generateToken(username);

        // Act
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertNotNull(extractedUsername);
        assertEquals(username, extractedUsername);
        assertTrue(extractedUsername.equals(username)); // Verify exact match
    }

    // Helper method
    private void assertNotEmpty(String value) {
        assertNotNull(value);
        assertTrue(value.length() > 0);
    }
}
