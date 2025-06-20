package it.pagopa.ecommerce.commons.documents.v2;

import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;

/**
 * Base class containing information for a refunded transaction
 *
 * @see TransactionRefundedData
 * @see TransactionRefundRequestedData
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document
@AllArgsConstructor
@NoArgsConstructor
@Generated
public abstract sealed class BaseTransactionRefundedData permits TransactionRefundErrorData,TransactionRefundRequestedData,TransactionRefundedData {
    /**
     * Transaction status before transaction being refunded
     */
    protected @NotNull TransactionStatusDto statusBeforeRefunded;
}
