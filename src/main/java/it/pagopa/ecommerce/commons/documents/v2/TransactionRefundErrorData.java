package it.pagopa.ecommerce.commons.documents.v2;

import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;

/**
 * Data class that contains information about a transaction for which an error
 * occurred performing refund operation
 *
 * @see BaseTransactionRefundedData
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document
@NoArgsConstructor
@Generated
public final class TransactionRefundErrorData extends BaseTransactionRefundedData {

    /**
     * Constructor
     *
     * @param statusBeforeRefunded the transaction before
     */
    public TransactionRefundErrorData(@NotNull TransactionStatusDto statusBeforeRefunded) {
        super(statusBeforeRefunded);
    }
}
