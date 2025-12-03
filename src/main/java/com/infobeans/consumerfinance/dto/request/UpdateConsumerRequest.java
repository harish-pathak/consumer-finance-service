package com.infobeans.consumerfinance.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for updating an existing consumer.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateConsumerRequest {

    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name must contain only letters, spaces, hyphens, and apostrophes")
    private String firstName;

    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name must contain only letters, spaces, hyphens, and apostrophes")
    private String lastName;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone must be a valid phone number")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 50, message = "National ID must not exceed 50 characters")
    private String nationalId;

    // Employment Details
    @Size(max = 200, message = "Employer name must not exceed 200 characters")
    private String employerName;

    @Size(max = 100, message = "Job position must not exceed 100 characters")
    private String position;

    @Past(message = "Employment start date must be in the past")
    private LocalDate employmentStartDate;

    // Income Details
    @DecimalMin(value = "0.0", inclusive = false, message = "Monthly income must be greater than 0")
    @Digits(integer = 15, fraction = 2, message = "Monthly income must be a valid amount")
    private BigDecimal monthlyIncome;

    @DecimalMin(value = "0.0", inclusive = false, message = "Annual income must be greater than 0")
    @Digits(integer = 15, fraction = 2, message = "Annual income must be a valid amount")
    private BigDecimal annualIncome;
}
