package it.pagopa.ecommerce.commons.documents;

import it.pagopa.generated.transactions.server.model.TransactionStatusDto;
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
