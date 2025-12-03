package com.infobeans.consumerfinance.exception;

/**
 * Exception thrown when a business rule is violated.
 * This is used for domain-specific business logic violations.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
public class BusinessRuleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String ruleCode;
    private final String ruleDescription;

    /**
     * Constructs a new BusinessRuleException with the specified detail message.
     *
     * @param message the detail message
     */
    public BusinessRuleException(String message) {
        super(message);
        this.ruleCode = null;
        this.ruleDescription = null;
    }

    /**
     * Constructs a new BusinessRuleException with rule details.
     *
     * @param ruleCode        the business rule code
     * @param ruleDescription the description of the business rule
     */
    public BusinessRuleException(String ruleCode, String ruleDescription) {
        super(String.format("Business rule violation [%s]: %s", ruleCode, ruleDescription));
        this.ruleCode = ruleCode;
        this.ruleDescription = ruleDescription;
    }

    /**
     * Constructs a new BusinessRuleException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
        this.ruleCode = null;
        this.ruleDescription = null;
    }

    /**
     * Constructs a new BusinessRuleException with rule details and cause.
     *
     * @param ruleCode        the business rule code
     * @param ruleDescription the description of the business rule
     * @param cause           the cause
     */
    public BusinessRuleException(String ruleCode, String ruleDescription, Throwable cause) {
        super(String.format("Business rule violation [%s]: %s", ruleCode, ruleDescription), cause);
        this.ruleCode = ruleCode;
        this.ruleDescription = ruleDescription;
    }

    /**
     * Gets the business rule code.
     *
     * @return the rule code
     */
    public String getRuleCode() {
        return ruleCode;
    }

    /**
     * Gets the business rule description.
     *
     * @return the rule description
     */
    public String getRuleDescription() {
        return ruleDescription;
    }
}
