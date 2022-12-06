package it.pagopa.ecommerce.commons.documents;

import it.pagopa.generated.transactions.server.model.AuthorizationResultDto;
import it.pagopa.generated.transactions.server.model.TransactionStatusDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Data related to authorization status update by a payment gateway
 */
@AllArgsConstructor
@Data
@Document
public class TransactionAuthorizationStatusUpdateData {

    private AuthorizationResultDto authorizationResult;
    private TransactionStatusDto newTransactionStatus;
}
