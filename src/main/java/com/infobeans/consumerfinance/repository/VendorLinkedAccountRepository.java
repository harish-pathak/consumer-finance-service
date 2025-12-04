package com.infobeans.consumerfinance.repository;

import com.infobeans.consumerfinance.domain.VendorLinkedAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for VendorLinkedAccount entity.
 *
 * Provides persistence operations for vendor-linked accounts including:
 * - Lookup by consumer ID (list all accounts for a consumer)
 * - Lookup by consumer ID and vendor ID (find specific vendor link)
 * - Lookup by ID (retrieve individual account)
 * - Existence checks for idempotent creation
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Repository
public interface VendorLinkedAccountRepository extends JpaRepository<VendorLinkedAccount, String> {

    /**
     * Find all vendor-linked accounts for a specific consumer.
     *
     * @param consumerId the consumer ID (UUID as String)
     * @return list of vendor-linked accounts for the consumer, empty list if none exist
     */
    List<VendorLinkedAccount> findByConsumerId(String consumerId);

    /**
     * Find a specific vendor-linked account by consumer ID and vendor ID.
     *
     * This method is used for idempotent creation and duplicate detection.
     * Returns the existing account if one already exists for the given consumer-vendor pair.
     *
     * @param consumerId the consumer ID (UUID as String)
     * @param vendorId the vendor ID (UUID as String)
     * @return Optional containing the vendor-linked account if it exists, empty otherwise
     */
    Optional<VendorLinkedAccount> findByConsumerIdAndVendorId(String consumerId, String vendorId);

    /**
     * Check if a vendor-linked account exists for a specific consumer-vendor pair.
     *
     * @param consumerId the consumer ID (UUID as String)
     * @param vendorId the vendor ID (UUID as String)
     * @return true if a vendor-linked account exists for this pair, false otherwise
     */
    boolean existsByConsumerIdAndVendorId(String consumerId, String vendorId);
}
