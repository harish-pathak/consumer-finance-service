package com.infobeans.consumerfinance.repository;

import com.infobeans.consumerfinance.domain.LoanApplication;
import com.infobeans.consumerfinance.domain.enums.LoanApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for LoanApplication entity.
 *
 * Provides CRUD operations and custom query methods for loan application data access.
 * Extends JpaRepository to leverage Spring Data JPA features like pagination, sorting, and transactions.
 *
 * Primary responsibilities:
 * - CRUD operations for loan applications
 * - Duplicate application detection (within time window)
 * - Query loan applications by consumer and status
 * - Support for loan application lifecycle management
 *
 * Duplicate Detection Strategy:
 * - Detects PENDING applications for the same consumer within a configurable time window
 * - Allows multiple historical applications but prevents concurrent duplicates
 * - Uses database-level queries with proper indexing on (consumer_id, status, created_at)
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, String> {

    /**
     * Find all loan applications for a given consumer.
     * Useful for retrieving application history for a consumer.
     *
     * Query pattern: SELECT * FROM loan_applications WHERE consumer_id = ?
     * Index used: idx_app_consumer (consumer_id)
     *
     * @param consumerId the consumer ID to search for
     * @return list of loan applications for the consumer (may be empty)
     */
    List<LoanApplication> findByConsumerId(String consumerId);

    /**
     * Find all PENDING loan applications for a given consumer.
     * Critical method for duplicate detection - prevents multiple concurrent applications.
     *
     * Query pattern: SELECT * FROM loan_applications WHERE consumer_id = ? AND status = 'PENDING'
     * Index used: idx_app_consumer_status (consumer_id, status)
     *
     * Business Rule:
     * - A consumer can only have ONE PENDING application at a time
     * - If a PENDING application exists, reject new submission with HTTP 409 (Conflict)
     * - Once application is APPROVED/REJECTED/CANCELLED, new submissions are allowed
     *
     * @param consumerId the consumer ID to search for
     * @return list of pending loan applications for the consumer
     */
    List<LoanApplication> findByConsumerIdAndStatus(String consumerId, LoanApplicationStatus status);

    /**
     * Check if a PENDING loan application exists for the given consumer.
     * Optimized query for existence check (used for duplicate detection).
     *
     * Query pattern: SELECT EXISTS(SELECT 1 FROM loan_applications WHERE consumer_id = ? AND status = 'PENDING')
     * Index used: idx_app_consumer_status (consumer_id, status)
     *
     * Performance: Very efficient with proper indexing, used in pre-submission validation.
     *
     * @param consumerId the consumer ID to check
     * @param status the status to check for
     * @return true if at least one application exists with the given consumer and status
     */
    boolean existsByConsumerIdAndStatus(String consumerId, LoanApplicationStatus status);

    /**
     * Find applications for a consumer by status.
     * Generic method for querying applications by status across all consumers or for a specific consumer.
     *
     * Query pattern: SELECT * FROM loan_applications WHERE status = ?
     * Index used: idx_app_status (status)
     *
     * @param status the application status to search for
     * @return list of loan applications with the given status
     */
    List<LoanApplication> findByStatus(LoanApplicationStatus status);

    /**
     * Find a specific application by ID.
     * Inherits from JpaRepository, provided for clarity.
     *
     * Query pattern: SELECT * FROM loan_applications WHERE id = ?
     * Index used: PRIMARY KEY (id)
     *
     * @param id the application ID
     * @return Optional containing the application if found, empty otherwise
     */
    @Override
    Optional<LoanApplication> findById(String id);

    /**
     * Count total loan applications for a consumer.
     * Useful for statistics and reporting.
     *
     * Query pattern: SELECT COUNT(*) FROM loan_applications WHERE consumer_id = ?
     * Index used: idx_app_consumer (consumer_id)
     *
     * @param consumerId the consumer ID
     * @return count of loan applications for the consumer
     */
    long countByConsumerId(String consumerId);

    /**
     * Find the most recent PENDING application for a consumer.
     * Used for detailed duplicate information in error responses.
     * Returns applications sorted by creation date in descending order, limited to 1 result.
     *
     * Query pattern: SELECT * FROM loan_applications
     *               WHERE consumer_id = ? AND status = 'PENDING'
     *               ORDER BY created_at DESC LIMIT 1
     * Index used: idx_app_consumer_status (consumer_id, status)
     *
     * @param consumerId the consumer ID to search for
     * @return Optional containing the most recent pending application if found
     */
    @Query("SELECT l FROM LoanApplication l WHERE l.consumerId = :consumerId AND l.status = :status ORDER BY l.createdAt DESC LIMIT 1")
    Optional<LoanApplication> findMostRecentByConsumerIdAndStatus(
        @Param("consumerId") String consumerId,
        @Param("status") LoanApplicationStatus status
    );
}
