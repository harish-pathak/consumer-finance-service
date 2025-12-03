package com.infobeans.consumerfinance.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for creating a new loan application.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLoanApplicationRequest {

    @NotNull(message = "Consumer ID is required")
    private UUID consumerId;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "100.0", message = "Loan amount must be at least 100")
    @DecimalMax(value = "1000000.0", message = "Loan amount must not exceed 1,000,000")
    @Digits(integer = 15, fraction = 2, message = "Loan amount must be a valid amount")
    private BigDecimal loanAmount;

    @NotNull(message = "Loan term is required")
    @Min(value = 1, message = "Loan term must be at least 1 month")
    @Max(value = 60, message = "Loan term must not exceed 60 months")
    private Integer termMonths;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.0", message = "Interest rate must be at least 0")
    @DecimalMax(value = "100.0", message = "Interest rate must not exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Interest rate must be a valid percentage")
    private BigDecimal interestRate;

    @NotBlank(message = "Purpose is required")
    @Size(max = 500, message = "Purpose must not exceed 500 characters")
    private String purpose;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
