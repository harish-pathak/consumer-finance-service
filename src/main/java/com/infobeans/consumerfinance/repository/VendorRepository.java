package com.infobeans.consumerfinance.repository;

import com.infobeans.consumerfinance.domain.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Vendor entity.
 *
 * Provides persistence operations for vendors.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Repository
public interface VendorRepository extends JpaRepository<Vendor, String> {
}
