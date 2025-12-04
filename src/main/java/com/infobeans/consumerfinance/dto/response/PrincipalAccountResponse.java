package com.infobeans.consumerfinance.dto.response;

import com.infobeans.consumerfinance.domain.enums.AccountStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for PrincipalAccount response.
 *
 * Represents the API response for principal account operations.
 * ID fields are returned as strings (UUID format) for consistency with the entity model.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrincipalAccountResponse {

    /**
     * Principal account ID (UUID as string).
     */
    private String id;

    /**
     * Consumer ID this principal account belongs to (UUID as string).
     */
    private String consumerId;
    private String accountType;
    private AccountStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
