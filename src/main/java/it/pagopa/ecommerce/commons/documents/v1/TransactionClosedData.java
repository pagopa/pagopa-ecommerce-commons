package it.pagopa.ecommerce.commons.documents.v1;

import it.pagopa.generated.ecommerce.nodo.v2.dto.ClosePaymentResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Data related to Nodo close payment outcome (OK/KO)
 */
@AllArgsConstructor
@Data
@Document
public class TransactionClosedData {

    /**
     * The Nodo close payment outcome
     */
    private ClosePaymentResponseDto.OutcomeEnum outcome;
}
