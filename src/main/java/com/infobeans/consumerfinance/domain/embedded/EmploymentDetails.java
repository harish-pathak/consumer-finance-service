package com.infobeans.consumerfinance.domain.embedded;

import com.infobeans.consumerfinance.converter.EncryptedFieldConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
public class EmploymentDetails {

    @Column(name = "employer_name", length = 500)
    @Convert(converter = EncryptedFieldConverter.class)
    private String employerName;

    @Column(name = "employment_type", length = 50)
    private String employmentType;  // FULL_TIME, PART_TIME, SELF_EMPLOYED, etc.

    @Column(name = "position", length = 100)
    private String position;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "years_of_experience")
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
