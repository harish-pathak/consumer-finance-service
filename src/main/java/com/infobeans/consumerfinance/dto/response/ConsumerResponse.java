package com.infobeans.consumerfinance.dto.response;

import com.infobeans.consumerfinance.domain.enums.AccountStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Consumer response.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerResponse {

    private UUID id;
    private String firstName;
    private String lastName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private String email;
    private String phone;
    private String nationalId; // Masked for security
    private String panNumber; // Masked for security (show only last 4 chars)

    // Employment Details
    private String employerName;
    private String position;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate employmentStartDate;

    // Income Details
    private BigDecimal monthlyIncome;
    private BigDecimal annualIncome;

    private AccountStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;
}
