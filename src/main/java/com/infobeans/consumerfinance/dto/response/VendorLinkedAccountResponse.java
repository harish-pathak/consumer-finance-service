package com.infobeans.consumerfinance.dto.response;

import com.infobeans.consumerfinance.domain.enums.AccountStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for VendorLinkedAccount response.
 *
 * @author Consumer Finance Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorLinkedAccountResponse {

    private UUID id;
    private UUID consumerId;
    private UUID principalAccountId;
    private UUID vendorId;
    private String vendorName;
    private AccountStatus status;
    private String externalAccountRef;
    private String linkageId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
