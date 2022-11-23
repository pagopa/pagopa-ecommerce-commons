package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.generated.nodo.v2.dto.ClosePaymentResponseDto;
import it.pagopa.ecommerce.commons.generated.transactions.model.TransactionStatusDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@Data
@Document
public class TransactionClosureSendData {

    private ClosePaymentResponseDto.OutcomeEnum nodeClosePaymentOutcome;
    private TransactionStatusDto newTransactionStatus;
    private String authorizationCode;
}
