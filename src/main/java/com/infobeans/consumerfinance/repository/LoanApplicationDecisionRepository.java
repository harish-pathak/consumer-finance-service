package com.infobeans.consumerfinance.repository;

import com.infobeans.consumerfinance.domain.LoanApplicationDecision;
import com.infobeans.consumerfinance.domain.enums.LoanDecisionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for LoanApplicationDecision entity.
 *
 * Manages persistence of decision audit records for loan applications.
 * Provides queries for duplicate detection and decision retrieval.
 *
 * Duplicate Prevention:
 * - Database unique constraint on (application_id, decision) prevents multiple decisions of same type
 * - Service layer checks for existing decision before attempting to create new one
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Repository
public interface LoanApplicationDecisionRepository extends JpaRepository<LoanApplicationDecision, String> {

    /**
     * Find all decisions made on a specific loan application.
     * Returns decisions in creation order (oldest first).
     *
     * @param applicationId the loan application ID
     * @return list of decisions for this application (empty if none exist)
     */
    List<LoanApplicationDecision> findByApplicationIdOrderByCreatedAtAsc(String applicationId);

    /**
     * Check if a specific decision already exists for an application.
     * Used for duplicate detection before creating a new decision.
     *
     * Example:
     * - existsByApplicationIdAndDecision(appId, APPROVED) checks if already approved
     * - existsByApplicationIdAndDecision(appId, REJECTED) checks if already rejected
     *
     * @param applicationId the loan application ID
     * @param decision the decision type (APPROVED or REJECTED)
     * @return true if decision of this type exists, false otherwise
     */
    boolean existsByApplicationIdAndDecision(String applicationId, LoanDecisionStatus decision);

    /**
     * Find a specific decision for an application if it exists.
     *
     * @param applicationId the loan application ID
     * @param decision the decision type to look for
     * @return Optional containing the decision if it exists, empty otherwise
     */
    Optional<LoanApplicationDecision> findByApplicationIdAndDecision(String applicationId, LoanDecisionStatus decision);

    /**
     * Find all decisions made by a specific staff member.
     * Useful for staff audit logs and dashboards.
     *
     * @param staffId the staff member ID/username
     * @return list of decisions made by this staff member
     */
    List<LoanApplicationDecision> findByStaffIdOrderByCreatedAtDesc(String staffId);

    /**
     * Count decisions made by a specific staff member.
     * Useful for staff performance metrics.
     *
     * @param staffId the staff member ID/username
     * @return count of decisions made by this staff member
     */
    long countByStaffId(String staffId);

    /**
     * Count approvals made by a specific staff member.
     *
     * @param staffId the staff member ID/username
     * @return count of approvals by this staff member
     */
    @Query("SELECT COUNT(d) FROM LoanApplicationDecision d WHERE d.staffId = :staffId AND d.decision = 'APPROVED'")
    long countApprovalsBy(@Param("staffId") String staffId);

    /**
     * Count rejections made by a specific staff member.
     *
     * @param staffId the staff member ID/username
     * @return count of rejections by this staff member
     */
    @Query("SELECT COUNT(d) FROM LoanApplicationDecision d WHERE d.staffId = :staffId AND d.decision = 'REJECTED'")
    long countRejectionsBy(@Param("staffId") String staffId);
}
