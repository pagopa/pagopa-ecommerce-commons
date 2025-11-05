package it.pagopa.ecommerce.commons.documents.v2;

import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Data related to expiration event
 */
@Data
@Document
@NoArgsConstructor
@Generated
public class TransactionExpiredData {

    private TransactionStatusDto statusBeforeExpiration;

    /**
     * All-args constructor
     *
     * @param statusBeforeExpiration the transaction status before expiration
     */
    public TransactionExpiredData(TransactionStatusDto statusBeforeExpiration) {
        this.statusBeforeExpiration = statusBeforeExpiration;
    }
}
