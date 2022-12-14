package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Data related to user receipt notification event.
 */
@AllArgsConstructor
@Data
@Document
public class TransactionAddReceiptData {

    private TransactionStatusDto newTransactionStatus;
}
