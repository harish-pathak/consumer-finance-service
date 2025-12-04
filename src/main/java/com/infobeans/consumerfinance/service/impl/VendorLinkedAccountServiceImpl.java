package com.infobeans.consumerfinance.service.impl;

import com.infobeans.consumerfinance.domain.Consumer;
import com.infobeans.consumerfinance.domain.Vendor;
import com.infobeans.consumerfinance.domain.VendorLinkedAccount;
import com.infobeans.consumerfinance.domain.enums.AccountStatus;
import com.infobeans.consumerfinance.dto.request.LinkVendorAccountRequest;
import com.infobeans.consumerfinance.dto.response.VendorLinkedAccountResponse;
import com.infobeans.consumerfinance.exception.BusinessRuleException;
import com.infobeans.consumerfinance.exception.ResourceNotFoundException;
import com.infobeans.consumerfinance.repository.ConsumerRepository;
import com.infobeans.consumerfinance.repository.VendorLinkedAccountRepository;
import com.infobeans.consumerfinance.repository.VendorRepository;
import com.infobeans.consumerfinance.service.VendorLinkedAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for vendor-linked account operations.
 *
 * Implements idempotent creation, retrieval, and lifecycle management of vendor-linked accounts.
 * Handles database-level uniqueness constraints and race conditions gracefully.
 *
 * Transaction Management:
 * - createOrLinkVendorAccount: Transactional with proper exception handling for race conditions
 * - updateVendorAccountStatus: Transactional for state transitions
 * - Other retrieval methods: ReadOnly transactions for performance
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VendorLinkedAccountServiceImpl implements VendorLinkedAccountService {

    private final VendorLinkedAccountRepository vendorLinkedAccountRepository;
    private final ConsumerRepository consumerRepository;
    private final VendorRepository vendorRepository;

    /**
     * Create or get an existing vendor-linked account (idempotent operation).
     *
     * Process:
     * 1. Validate that both consumer and vendor exist
     * 2. Check if account already exists for this consumer-vendor pair
     * 3. If exists, return existing account (idempotent)
     * 4. If not exists, create new account
     * 5. Handle DataIntegrityViolationException (race condition) by retrieving existing account
     *
     * @param request LinkVendorAccountRequest with consumer ID, vendor ID, and optional fields
     * @return VendorLinkedAccountResponse with created or existing account
     * @throws ResourceNotFoundException if consumer or vendor not found
     */
    @Override
    @Transactional
    public VendorLinkedAccountResponse createOrLinkVendorAccount(LinkVendorAccountRequest request) {
        String consumerId = request.getConsumerId();
        String vendorId = request.getVendorId();

        log.info("Processing vendor account link request for consumer: {} and vendor: {}", consumerId, vendorId);

        // Step 1: Validate consumer exists
        Consumer consumer = consumerRepository.findById(consumerId)
            .orElseThrow(() -> new ResourceNotFoundException("Consumer", "id", consumerId));

        // Step 2: Validate vendor exists
        Vendor vendor = vendorRepository.findById(vendorId)
            .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));

        // Step 3: Check if account already exists (idempotent)
        var existingAccount = vendorLinkedAccountRepository.findByConsumerIdAndVendorId(consumerId, vendorId);
        if (existingAccount.isPresent()) {
            log.info("Vendor-linked account already exists for consumer: {} and vendor: {}. Returning existing account.",
                consumerId, vendorId);
            return mapToResponse(existingAccount.get(), vendor.getName());
        }

        // Step 4: Create new vendor-linked account
        VendorLinkedAccount account = VendorLinkedAccount.builder()
            .consumerId(consumerId)
            .vendorId(vendorId)
            .status(AccountStatus.ACTIVE)
            .externalAccountRef(request.getExternalAccountRef())
            .linkageId(request.getLinkageId())
            .build();

        try {
            VendorLinkedAccount saved = vendorLinkedAccountRepository.save(account);
            log.info("Vendor-linked account created successfully for consumer: {} and vendor: {}",
                consumerId, vendorId);
            return mapToResponse(saved, vendor.getName());

        } catch (DataIntegrityViolationException e) {
            // Handle race condition: account created between existence check and save
            log.warn("Race condition detected during vendor-linked account creation. " +
                "Another request created the same account. Retrieving existing account for consumer: {} and vendor: {}",
                consumerId, vendorId);

            var raceConditionAccount = vendorLinkedAccountRepository.findByConsumerIdAndVendorId(consumerId, vendorId)
                .orElseThrow(() -> new RuntimeException(
                    "Unable to retrieve vendor-linked account after DataIntegrityViolationException", e));

            return mapToResponse(raceConditionAccount, vendor.getName());
        }
    }

    /**
     * Get a specific vendor-linked account by ID.
     *
     * @param accountId the vendor-linked account ID (UUID as String)
     * @return VendorLinkedAccountResponse with account details
     * @throws ResourceNotFoundException if account not found
     */
    @Override
    @Transactional(readOnly = true)
    public VendorLinkedAccountResponse getVendorLinkedAccountById(String accountId) {
        VendorLinkedAccount account = vendorLinkedAccountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("VendorLinkedAccount", "id", accountId));

        // Fetch vendor name for enriched response
        String vendorName = vendorRepository.findById(account.getVendorId())
            .map(Vendor::getName)
            .orElse("Unknown");

        return mapToResponse(account, vendorName);
    }

    /**
     * List all vendor-linked accounts for a consumer.
     *
     * @param consumerId the consumer ID (UUID as String)
     * @return List of VendorLinkedAccountResponse objects
     * @throws ResourceNotFoundException if consumer not found
     */
    @Override
    @Transactional(readOnly = true)
    public List<VendorLinkedAccountResponse> listVendorAccountsByConsumerId(String consumerId) {
        // Validate consumer exists
        consumerRepository.findById(consumerId)
            .orElseThrow(() -> new ResourceNotFoundException("Consumer", "id", consumerId));

        List<VendorLinkedAccount> accounts = vendorLinkedAccountRepository.findByConsumerId(consumerId);

        log.info("Retrieved {} vendor-linked accounts for consumer: {}", accounts.size(), consumerId);

        // Map to response DTOs with vendor names
        return accounts.stream()
            .map(account -> {
                String vendorName = vendorRepository.findById(account.getVendorId())
                    .map(Vendor::getName)
                    .orElse("Unknown");
                return mapToResponse(account, vendorName);
            })
            .collect(Collectors.toList());
    }

    /**
     * Update the lifecycle status of a vendor-linked account.
     *
     * Supports state transitions:
     * - ACTIVE can transition to DISABLED or ARCHIVED
     * - DISABLED can transition to ACTIVE or ARCHIVED
     * - ARCHIVED is a terminal state (no transitions allowed)
     *
     * @param accountId the vendor-linked account ID (UUID as String)
     * @param newStatus the target status
     * @param updatedBy optional identifier of who/what triggered the update
     * @return VendorLinkedAccountResponse with updated account
     * @throws ResourceNotFoundException if account not found
     * @throws BusinessRuleException if state transition is invalid or from terminal state
     */
    @Override
    @Transactional
    public VendorLinkedAccountResponse updateVendorAccountStatus(String accountId, AccountStatus newStatus, String updatedBy) {
        VendorLinkedAccount account = vendorLinkedAccountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("VendorLinkedAccount", "id", accountId));

        AccountStatus currentStatus = account.getStatus();

        // Validate state transition
        validateStatusTransition(currentStatus, newStatus);

        log.info("Updating vendor-linked account {} status from {} to {}", accountId, currentStatus, newStatus);

        // Update status and audit fields
        account.setStatus(newStatus);
        account.setUpdatedBy(updatedBy);

        VendorLinkedAccount updated = vendorLinkedAccountRepository.save(account);

        log.info("Vendor-linked account {} status updated successfully to {}", accountId, newStatus);

        // Fetch vendor name for enriched response
        String vendorName = vendorRepository.findById(updated.getVendorId())
            .map(Vendor::getName)
            .orElse("Unknown");

        return mapToResponse(updated, vendorName);
    }

    /**
     * Check if a vendor-linked account exists for a specific consumer-vendor pair.
     *
     * @param consumerId the consumer ID (UUID as String)
     * @param vendorId the vendor ID (UUID as String)
     * @return true if account exists, false otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public boolean vendorAccountExists(String consumerId, String vendorId) {
        return vendorLinkedAccountRepository.existsByConsumerIdAndVendorId(consumerId, vendorId);
    }

    /**
     * Validate that the requested status transition is allowed.
     *
     * Rules:
     * - From ACTIVE: can go to DISABLED or ARCHIVED
     * - From DISABLED: can go to ACTIVE or ARCHIVED
     * - From ARCHIVED: no transitions allowed (terminal state)
     *
     * @param currentStatus the current account status
     * @param newStatus the requested target status
     * @throws BusinessRuleException if transition is invalid
     */
    private void validateStatusTransition(AccountStatus currentStatus, AccountStatus newStatus) {
        // Same status transition is not an error, just a no-op
        if (currentStatus == newStatus) {
            return;
        }

        // ARCHIVED is a terminal state
        if (currentStatus == AccountStatus.ARCHIVED) {
            throw new BusinessRuleException(
                "Cannot transition vendor-linked account status from ARCHIVED. ARCHIVED is a terminal state."
            );
        }

        // All other transitions are allowed (ACTIVE<->DISABLED, ACTIVE->ARCHIVED, DISABLED->ARCHIVED)
        log.debug("Status transition from {} to {} is allowed", currentStatus, newStatus);
    }

    /**
     * Map VendorLinkedAccount entity to VendorLinkedAccountResponse DTO.
     *
     * @param account the source entity
     * @param vendorName the vendor name to include in response
     * @return VendorLinkedAccountResponse
     */
    private VendorLinkedAccountResponse mapToResponse(VendorLinkedAccount account, String vendorName) {
        return VendorLinkedAccountResponse.builder()
            .id(account.getId())
            .consumerId(account.getConsumerId())
            .principalAccountId(account.getPrincipalAccountId())
            .vendorId(account.getVendorId())
            .vendorName(vendorName)
            .status(account.getStatus())
            .externalAccountRef(account.getExternalAccountRef())
            .linkageId(account.getLinkageId())
            .createdAt(account.getCreatedAt())
            .updatedAt(account.getUpdatedAt())
            .build();
    }
}
