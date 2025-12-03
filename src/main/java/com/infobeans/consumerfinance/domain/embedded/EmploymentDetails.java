package com.infobeans.consumerfinance.domain.embedded;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded Entity for Employment Details
 *
 * Stores employment-related information for a consumer.
 * This is an embedded object within the Consumer entity.
 * Sensitive field - should be encrypted at application level.
 *
 * @author Harish Pathak
 * @since 1.0.0
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmploymentDetails {

    private String employerName;
    private String employmentType;  // FULL_TIME, PART_TIME, SELF_EMPLOYED, etc.
    private String position;
    private String industry;
    private Long yearsOfExperience;

    @Override
    public String toString() {
        return "EmploymentDetails{" +
                "employerName='" + employerName + '\'' +
                ", employmentType='" + employmentType + '\'' +
                ", position='" + position + '\'' +
                ", industry='" + industry + '\'' +
                ", yearsOfExperience=" + yearsOfExperience +
                '}';
    }
}
