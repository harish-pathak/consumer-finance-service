package com.infobeans.consumerfinance.domain.embedded;

import com.infobeans.consumerfinance.converter.EncryptedFieldConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

    @Column(name = "monthly_income", precision = 15, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "annual_income", precision = 15, scale = 2)
    private BigDecimal annualIncome;

    @Column(name = "income_source", length = 500)
    @Convert(converter = EncryptedFieldConverter.class)
    private String incomeSource;  // SALARY, BUSINESS, RENT, etc.

    @Column(name = "currency", length = 10)
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
