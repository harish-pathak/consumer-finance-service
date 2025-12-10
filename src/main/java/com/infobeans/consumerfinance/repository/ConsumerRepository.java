package com.infobeans.consumerfinance.repository;

import com.infobeans.consumerfinance.domain.Consumer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Consumer entity.
 *
 * Provides CRUD operations and custom query methods for consumer data access.
 * Extends JpaRepository to leverage Spring Data JPA features like pagination, sorting, and transactions.
 *
 * Note: Consumer entity uses String IDs (UUID as string), not UUID objects.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Repository
public interface ConsumerRepository extends JpaRepository<Consumer, String> {

    /**
     * Find a consumer by email address.
     * Used for duplicate detection during onboarding.
     *
     * @param email the email address to search for
     * @return Optional containing the consumer if found
     */
    Optional<Consumer> findByEmail(String email);

    /**
     * Find a consumer by national ID.
     * Used for identity verification and duplicate detection.
     *
     * @param nationalId the national ID to search for (encrypted in database)
     * @return Optional containing the consumer if found
     */
    Optional<Consumer> findByNationalId(String nationalId);

    /**
     * Find consumers by last name with pagination support.
     *
     * @param lastName the last name to search for
     * @param pageable pagination information
     * @return Page of consumers matching the last name
     */
    Page<Consumer> findByLastName(String lastName, Pageable pageable);

    /**
     * Find consumers by first and last name combination.
     *
     * @param firstName the first name to search for
     * @param lastName the last name to search for
     * @param pageable pagination information
     * @return Page of consumers matching the name combination
     */
    Page<Consumer> findByFirstNameAndLastName(String firstName, String lastName, Pageable pageable);

    /**
     * Check if a consumer exists with the given email.
     * Optimized query for duplicate detection.
     *
     * @param email the email address to check
     * @return true if a consumer with this email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if a consumer exists with the given national ID.
     * Optimized query for duplicate detection.
     *
     * @param nationalId the national ID to check (encrypted in database)
     * @return true if a consumer with this national ID exists, false otherwise
     */
    boolean existsByNationalId(String nationalId);

    /**
     * Check if a consumer exists with the given phone number.
     * Optimized query for duplicate detection.
     *
     * @param phone the phone number to check
     * @return true if a consumer with this phone number exists, false otherwise
     */
    boolean existsByPhone(String phone);

    /**
     * Check if a consumer exists with the given document number.
     * Optimized query for duplicate detection.
     *
     * @param documentNumber the document number to check
     * @return true if a consumer with this document number exists, false otherwise
     */
    boolean existsByDocumentNumber(String documentNumber);

    /**
     * Custom query to find consumers by email or phone number.
     * Used for flexible consumer lookup.
     *
     * @param email the email address or phone number to search for
     * @return Page of consumers matching the search criteria
     */
    @Query("SELECT c FROM Consumer c WHERE c.email = :email OR c.phone = :phone")
    Optional<Consumer> findByEmailOrPhone(@Param("email") String email, @Param("phone") String phone);
}
