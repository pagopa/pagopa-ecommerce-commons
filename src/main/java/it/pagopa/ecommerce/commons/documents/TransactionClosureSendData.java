package it.pagopa.ecommerce.commons.documents;

import it.pagopa.generated.ecommerce.nodo.v2.dto.ClosePaymentResponseDto;
import it.pagopa.generated.transactions.server.model.TransactionStatusDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Data related to closure of a transaction
 */
@AllArgsConstructor
@Data
@Document
public class TransactionClosureSendData {

    private ClosePaymentResponseDto.OutcomeEnum nodeClosePaymentOutcome;
    private TransactionStatusDto newTransactionStatus;
    private String authorizationCode;
}
