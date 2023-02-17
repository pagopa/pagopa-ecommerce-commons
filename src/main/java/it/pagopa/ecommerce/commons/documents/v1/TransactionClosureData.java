package it.pagopa.ecommerce.commons.documents.v1;

import it.pagopa.generated.ecommerce.nodo.v2.dto.ClosePaymentResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

/**
 * Data related to Nodo close payment operation such as outcome (OK/KO)
 */
@AllArgsConstructor
@Data
@Document
public class TransactionClosureData {

    /**
     * The Nodo close payment outcome
     */
    @Nullable
    private ClosePaymentResponseDto.OutcomeEnum outcome;
}
