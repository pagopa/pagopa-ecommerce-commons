package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.generated.transactions.model.TransactionStatusDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@Data
@Document
public class TransactionAddReceiptData {

    private TransactionStatusDto newTransactionStatus;
}
