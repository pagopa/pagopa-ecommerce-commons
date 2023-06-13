package it.pagopa.ecommerce.commons.documents.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

/**
 * Data related to Nodo close payment operation such as outcome (OK/KO)
 */
@AllArgsConstructor
@Data
@Document
public class TransactionClosureData {

    /**
     * The Nodo closePayment outcome
     */
    @NotNull
    private Outcome responseOutcome;

    /**
     * The Nodo closePayment request timestamp
     */
    @Nullable
    private OffsetDateTime timestamp;

    /**
     * Enumeration of Nodo closePayment outcome
     */
    public enum Outcome {
        /**
         * closePayment OK outcome
         */
        OK,
        /**
         * closePayment KO outcome
         */
        KO
    }
}
