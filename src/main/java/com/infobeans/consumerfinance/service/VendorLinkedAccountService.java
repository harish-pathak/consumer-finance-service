package com.infobeans.consumerfinance.service;

import com.infobeans.consumerfinance.domain.enums.AccountStatus;
import com.infobeans.consumerfinance.dto.request.LinkVendorAccountRequest;
import com.infobeans.consumerfinance.dto.response.VendorLinkedAccountResponse;

import java.util.List;

/**
 * Service interface for vendor-linked account operations.
 *
 * Provides business logic for managing vendor-linked accounts including:
 * - Idempotent creation/linking of vendor accounts
 * - Retrieval of vendor-linked accounts for consumers
 * - Lifecycle management (status transitions)
 *
 * Key characteristics:
 * - Idempotent creation: calling create multiple times with same consumer-vendor pair returns existing record
 * - Enforces uniqueness at both application and database levels
 * - Handles race conditions via database unique constraint
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
public interface VendorLinkedAccountService {

    /**
     * Create or get an existing vendor-linked account (idempotent operation).
     *
     * This method ensures only one vendor-linked account exists per consumer-vendor pair.
     * If an account already exists for the given consumer and vendor, the existing record is returned.
     * If not exists, a new account is created atomically within a transaction.
     *
     * Database-level uniqueness constraint ensures protection against race conditions:
     * if two concurrent requests attempt to create the same account, one succeeds and
     * the other receives a DataIntegrityViolationException, which is caught and converted
     * to return the existing account (consistent with idempotent semantics).
     *
     * @param request LinkVendorAccountRequest containing consumer ID, vendor ID, and optional metadata
     * @return VendorLinkedAccountResponse with created or existing account details
     * @throws com.infobeans.consumerfinance.exception.ResourceNotFoundException if consumer or vendor not found
     * @throws com.infobeans.consumerfinance.exception.ValidationException if request validation fails
     */
    VendorLinkedAccountResponse createOrLinkVendorAccount(LinkVendorAccountRequest request);

    /**
     * Get a specific vendor-linked account by ID.
     *
     * @param accountId the ID of the vendor-linked account (UUID as String)
     * @return VendorLinkedAccountResponse with account details
     * @throws com.infobeans.consumerfinance.exception.ResourceNotFoundException if account not found
     */
    VendorLinkedAccountResponse getVendorLinkedAccountById(String accountId);

    /**
     * List all vendor-linked accounts for a consumer.
     *
     * Returns all vendor-linked accounts belonging to the specified consumer,
     * regardless of their status (ACTIVE, DISABLED, ARCHIVED).
     * Results are typically sorted by creation date (most recent first).
     *
     * @param consumerId the consumer ID (UUID as String)
     * @return List of VendorLinkedAccountResponse objects; empty list if consumer has no linked accounts
     * @throws com.infobeans.consumerfinance.exception.ResourceNotFoundException if consumer not found
     */
    List<VendorLinkedAccountResponse> listVendorAccountsByConsumerId(String consumerId);

    /**
     * Update the lifecycle status of a vendor-linked account.
     *
     * Allows system/internal services to transition vendor-linked accounts between lifecycle states:
     * ACTIVE -> DISABLED -> ARCHIVED, or ACTIVE -> ARCHIVED directly.
     * Validation of allowed state transitions should be enforced.
     *
     * This operation updates the updatedAt timestamp and optionally records updatedBy metadata.
     *
     * @param accountId the ID of the vendor-linked account (UUID as String)
     * @param newStatus the target status (ACTIVE, DISABLED, or ARCHIVED)
     * @param updatedBy optional identifier of who/what triggered this update (user ID or system name)
     * @return VendorLinkedAccountResponse with updated account details
     * @throws com.infobeans.consumerfinance.exception.ResourceNotFoundException if account not found
     * @throws com.infobeans.consumerfinance.exception.BusinessRuleException if state transition is invalid
     */
    VendorLinkedAccountResponse updateVendorAccountStatus(String accountId, AccountStatus newStatus, String updatedBy);

    /**
     * Check if a vendor-linked account exists for a specific consumer-vendor pair.
     *
     * @param consumerId the consumer ID (UUID as String)
     * @param vendorId the vendor ID (UUID as String)
     * @return true if account exists, false otherwise
     */
    boolean vendorAccountExists(String consumerId, String vendorId);
}
