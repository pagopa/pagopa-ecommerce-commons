package it.pagopa.ecommerce.commons.documents.v1;

import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Data related to refund event
 */
@Data
@Document
@NoArgsConstructor
@Generated
public class TransactionRefundedData {

    private TransactionStatusDto statusBeforeRefunded;

    /**
     * All-args constructor
     *
     * @param statusBeforeRefunded the transaction status before the refund
     *                             operation
     */
    public TransactionRefundedData(TransactionStatusDto statusBeforeRefunded) {
        this.statusBeforeRefunded = statusBeforeRefunded;
    }
}
