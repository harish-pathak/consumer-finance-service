package com.infobeans.consumerfinance.domain.embedded;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Embedded Entity for Income Details
 *
 * Stores income-related information for a consumer.
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
public class IncomeDetails {

    private BigDecimal monthlyIncome;
    private BigDecimal annualIncome;
    private String incomeSource;  // SALARY, BUSINESS, RENT, etc.
    private String currency;      // USD, EUR, INR, etc.

    @Override
    public String toString() {
        return "IncomeDetails{" +
                "monthlyIncome=" + monthlyIncome +
                ", annualIncome=" + annualIncome +
                ", incomeSource='" + incomeSource + '\'' +
                ", currency='" + currency + '\'' +
                '}';
    }
}
