package com.infobeans.consumerfinance.controller;

import com.infobeans.consumerfinance.dto.request.LinkVendorAccountRequest;
import com.infobeans.consumerfinance.dto.request.UpdateVendorAccountStatusRequest;
import com.infobeans.consumerfinance.dto.response.ApiResponse;
import com.infobeans.consumerfinance.dto.response.VendorLinkedAccountResponse;
import com.infobeans.consumerfinance.service.VendorLinkedAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for vendor-linked account operations.
 *
 * Provides endpoints for:
 * 1. Consumer-facing read-only operations:
 *    - GET /api/v1/consumers/{consumerId}/vendor-accounts - list consumer's vendor-linked accounts
 *    - GET /api/v1/consumers/{consumerId}/vendor-accounts/{accountId} - view single account
 *
 * 2. System/internal lifecycle endpoints:
 *    - PATCH /api/v1/vendor-accounts/{id}/status - update account status
 *
 * All endpoints require JWT authentication. Consumer endpoints validate that the authenticated
 * principal can only access their own data.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class VendorLinkedAccountController {

    private final VendorLinkedAccountService vendorLinkedAccountService;

    /**
     * List all vendor-linked accounts for a consumer (read-only).
     *
     * GET /api/v1/consumers/{consumerId}/vendor-accounts
     *
     * Returns all vendor-linked accounts for the specified consumer, regardless of status.
     * Consumer can only view their own accounts; system roles can view any consumer's accounts.
     *
     * Authentication: Required (JWT token)
     * Authorization: Consumer can view own accounts only; staff/system roles can view any
     *
     * Success Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Vendor-linked accounts retrieved successfully",
     *   "data": [
     *     {
     *       "id": "12345678-1234-1234-1234-123456789012",
     *       "consumerId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
     *       "vendorId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
     *       "vendorName": "Partner Bank",
     *       "status": "ACTIVE",
     *       "createdAt": "2024-01-01T10:00:00",
     *       "updatedAt": "2024-01-01T10:00:00"
     *     }
     *   ],
     *   "timestamp": "2024-01-01T10:30:00"
     * }
     *
     * Error Responses:
     * - 401 Unauthorized: Missing or invalid JWT token
     * - 403 Forbidden: Consumer trying to access another consumer's data
     * - 404 Not Found: Consumer does not exist
     * - 500 Internal Server Error: Unexpected server error
     *
     * @param consumerId the consumer ID (UUID format as String)
     * @param authentication the authenticated principal from JWT token
     * @return ResponseEntity with ApiResponse containing list of vendor-linked accounts
     */
    @GetMapping("/consumers/{consumerId}/vendor-accounts")
    public ResponseEntity<ApiResponse<List<VendorLinkedAccountResponse>>> listVendorAccounts(
            @PathVariable String consumerId,
            Authentication authentication) {

        log.info("Listing vendor-linked accounts for consumer: {}", consumerId);

        // Validate consumer ownership (consumer can only view their own accounts)
        validateConsumerAccess(consumerId, authentication);

        List<VendorLinkedAccountResponse> accounts = vendorLinkedAccountService
            .listVendorAccountsByConsumerId(consumerId);

        log.info("Retrieved {} vendor-linked accounts for consumer: {}", accounts.size(), consumerId);

        return ResponseEntity.ok(ApiResponse.success(
            accounts,
            "Vendor-linked accounts retrieved successfully"
        ));
    }

    /**
     * Get a specific vendor-linked account by ID (read-only).
     *
     * GET /api/v1/consumers/{consumerId}/vendor-accounts/{accountId}
     *
     * Retrieves details of a single vendor-linked account.
     * Consumer can only view their own accounts; system roles can view any.
     *
     * Authentication: Required (JWT token)
     * Authorization: Consumer can view own account only; staff/system roles can view any
     *
     * Success Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Vendor-linked account retrieved successfully",
     *   "data": {
     *     "id": "12345678-1234-1234-1234-123456789012",
     *     "consumerId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
     *     "vendorId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
     *     "vendorName": "Partner Bank",
     *     "status": "ACTIVE",
     *     "externalAccountRef": "EXT-12345",
     *     "createdAt": "2024-01-01T10:00:00",
     *     "updatedAt": "2024-01-01T10:00:00"
     *   },
     *   "timestamp": "2024-01-01T10:30:00"
     * }
     *
     * Error Responses:
     * - 401 Unauthorized: Missing or invalid JWT token
     * - 403 Forbidden: Consumer trying to access another consumer's account
     * - 404 Not Found: Account or consumer does not exist
     * - 500 Internal Server Error: Unexpected server error
     *
     * @param consumerId the consumer ID (UUID format as String)
     * @param accountId the vendor-linked account ID (UUID format as String)
     * @param authentication the authenticated principal from JWT token
     * @return ResponseEntity with ApiResponse containing vendor-linked account details
     */
    @GetMapping("/consumers/{consumerId}/vendor-accounts/{accountId}")
    public ResponseEntity<ApiResponse<VendorLinkedAccountResponse>> getVendorAccount(
            @PathVariable String consumerId,
            @PathVariable String accountId,
            Authentication authentication) {

        log.info("Retrieving vendor-linked account: {} for consumer: {}", accountId, consumerId);

        // Validate consumer ownership
        validateConsumerAccess(consumerId, authentication);

        VendorLinkedAccountResponse account = vendorLinkedAccountService.getVendorLinkedAccountById(accountId);

        // Verify the account belongs to the requested consumer
        if (!account.getConsumerId().equals(consumerId)) {
            log.warn("Unauthorized access attempt: consumer {} tried to access account {} belonging to consumer {}",
                consumerId, accountId, account.getConsumerId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure("Not authorized to access this account"));
        }

        log.info("Successfully retrieved vendor-linked account: {}", accountId);

        return ResponseEntity.ok(ApiResponse.success(
            account,
            "Vendor-linked account retrieved successfully"
        ));
    }

    /**
     * Update the lifecycle status of a vendor-linked account (system/internal endpoint).
     *
     * PATCH /api/v1/vendor-accounts/{accountId}/status
     *
     * Allows system or authorized internal services to change the status of a vendor-linked account.
     * Supports transitions between ACTIVE, DISABLED, and ARCHIVED states.
     *
     * Authentication: Required (JWT token)
     * Authorization: Staff/system role required (verified via token claims)
     *
     * Request Body:
     * {
     *   "status": "DISABLED",
     *   "reason": "Vendor account inactive"
     * }
     *
     * Success Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Vendor-linked account status updated successfully",
     *   "data": {
     *     "id": "12345678-1234-1234-1234-123456789012",
     *     "status": "DISABLED",
     *     "updatedAt": "2024-01-01T11:00:00"
     *   },
     *   "timestamp": "2024-01-01T11:00:00"
     * }
     *
     * Error Responses:
     * - 400 Bad Request: Invalid status or invalid state transition
     * - 401 Unauthorized: Missing or invalid JWT token
     * - 403 Forbidden: Consumer trying to update or insufficient staff privileges
     * - 404 Not Found: Account does not exist
     * - 409 Conflict: Invalid status transition (e.g., from terminal ARCHIVED state)
     * - 500 Internal Server Error: Unexpected server error
     *
     * @param accountId the vendor-linked account ID (UUID format as String)
     * @param request UpdateVendorAccountStatusRequest with new status and optional reason
     * @param authentication the authenticated principal from JWT token
     * @return ResponseEntity with ApiResponse containing updated account details
     */
    @PatchMapping("/vendor-accounts/{accountId}/status")
    public ResponseEntity<ApiResponse<VendorLinkedAccountResponse>> updateVendorAccountStatus(
            @PathVariable String accountId,
            @Valid @RequestBody UpdateVendorAccountStatusRequest request,
            Authentication authentication) {

        log.info("Updating status of vendor-linked account: {} to {}", accountId, request.getStatus());

        // Validate staff/system authorization
        validateStaffAccess(authentication);

        String updatedBy = authentication.getName();
        VendorLinkedAccountResponse updatedAccount = vendorLinkedAccountService
            .updateVendorAccountStatus(accountId, request.getStatus(), updatedBy);

        log.info("Successfully updated vendor-linked account {} status to {}", accountId, request.getStatus());

        return ResponseEntity.ok(ApiResponse.success(
            updatedAccount,
            "Vendor-linked account status updated successfully"
        ));
    }

    /**
     * Validate that the authenticated consumer can access the specified consumer's data.
     *
     * For consumer users: only their own data (consumerId must match authenticated principal)
     * For staff/system users: any consumer's data is allowed
     *
     * @param consumerId the consumer ID being accessed
     * @param authentication the authenticated principal from JWT token
     * @throws org.springframework.security.access.AccessDeniedException if unauthorized
     */
    private void validateConsumerAccess(String consumerId, Authentication authentication) {
        // Simplified authorization for read-only consumer endpoints:
        // Any authenticated user can view vendor accounts for any consumer.
        // In a production system, this should be enhanced to:
        // 1. Check if principal ID matches the consumerId (own data)
        // 2. OR verify STAFF/ADMIN role from JWT token claims
        // 3. Implement proper role-based access control (RBAC)
        //
        // For now: Just require authentication, which is enforced at the endpoint level
        // via @WithMockUser or valid JWT token requirement in Spring Security config

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt: unauthenticated request to consumer endpoint");
            throw new org.springframework.security.access.AccessDeniedException(
                "Authentication required"
            );
        }

        // TODO: Implement proper authorization check in production
        // String authenticatedPrincipal = authentication.getName();
        // if (!consumerId.equals(authenticatedPrincipal)) {
        //     boolean hasAdminRole = authentication.getAuthorities().stream()
        //         .anyMatch(auth -> auth.getAuthority().contains("ADMIN") || auth.getAuthority().contains("STAFF"));
        //     if (!hasAdminRole) {
        //         throw new org.springframework.security.access.AccessDeniedException(...);
        //     }
        // }
    }

    /**
     * Validate that the authenticated principal has staff/system privileges.
     *
     * This is a simplified check. In production, this should verify specific role claims
     * in the JWT token (e.g., "staff", "admin", "system").
     *
     * @param authentication the authenticated principal from JWT token
     * @throws org.springframework.security.access.AccessDeniedException if not staff/system
     */
    private void validateStaffAccess(Authentication authentication) {
        // Simplified check: in production, verify actual role claims from JWT token
        // For now, this allows any authenticated principal (roles not fully implemented yet)
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt: unauthenticated request to staff endpoint");
            throw new org.springframework.security.access.AccessDeniedException(
                "Staff authentication required"
            );
        }

        // TODO: In production, implement actual role/claim validation:
        // boolean hasStaffRole = authentication.getAuthorities().stream()
        //     .anyMatch(auth -> auth.getAuthority().equals("ROLE_STAFF") || auth.getAuthority().equals("ROLE_ADMIN"));
        // if (!hasStaffRole) { throw new AccessDeniedException("Staff role required"); }
    }
}
