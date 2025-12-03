package com.infobeans.consumerfinance.exception;

/**
 * Exception thrown when a user attempts to access a resource without proper authorization.
 * This is used for authentication and authorization failures.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
public class UnauthorizedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String action;
    private final String resource;

    /**
     * Constructs a new UnauthorizedException with the specified detail message.
     *
     * @param message the detail message
     */
    public UnauthorizedException(String message) {
        super(message);
        this.action = null;
        this.resource = null;
    }

    /**
     * Constructs a new UnauthorizedException with action and resource details.
     *
     * @param action   the action that was attempted
     * @param resource the resource that was accessed
     */
    public UnauthorizedException(String action, String resource) {
        super(String.format("Unauthorized to %s on resource: %s", action, resource));
        this.action = action;
        this.resource = resource;
    }

    /**
     * Constructs a new UnauthorizedException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
        this.action = null;
        this.resource = null;
    }

    /**
     * Gets the action that was attempted.
     *
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * Gets the resource that was accessed.
     *
     * @return the resource
     */
    public String getResource() {
        return resource;
    }
}
