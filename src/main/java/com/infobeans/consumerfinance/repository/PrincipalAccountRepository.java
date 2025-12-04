package com.infobeans.consumerfinance.repository;

import com.infobeans.consumerfinance.domain.PrincipalAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for PrincipalAccount entity.
 *
 * Provides CRUD operations and custom query methods for principal account data access.
 * Extends JpaRepository to leverage Spring Data JPA features like pagination, sorting, and transactions.
 *
 * Uniqueness constraint: One principal account per consumer (enforced at DB level via unique index on consumer_id).
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Repository
public interface PrincipalAccountRepository extends JpaRepository<PrincipalAccount, String> {

    /**
     * Find a principal account by consumer ID.
     * Efficient lookup for retrieving a consumer's principal account (unique relationship).
     *
     * @param consumerId the consumer ID to search for
     * @return Optional containing the principal account if found, empty otherwise
     */
    Optional<PrincipalAccount> findByConsumerId(String consumerId);

    /**
     * Check if a principal account exists for the given consumer ID.
     * Optimized query for existence check (used for duplicate detection).
     *
     * @param consumerId the consumer ID to check
     * @return true if a principal account exists for this consumer, false otherwise
     */
    boolean existsByConsumerId(String consumerId);
}
