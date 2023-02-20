package it.pagopa.ecommerce.commons.documents.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

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
    private Outcome outcome;

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
