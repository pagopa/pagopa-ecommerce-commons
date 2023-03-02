package it.pagopa.ecommerce.commons.documents.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

/**
 * Data related to Nodo send payment result operation such as outcome (OK/KO)
 */
@AllArgsConstructor
@Data
@Document
public class TransactionUserReceiptData {

    /**
     * The Nodo sendPaymentResult outcome
     */
    @NotNull
    private Outcome responseOutcome;

    /**
     * Enumeration of Nodo sendPaymentResult outcome
     */
    public enum Outcome {
        /**
         * sendPaymentResult OK outcome
         */
        OK,
        /**
         * sendPaymentResult KO outcome
         */
        KO
    }
}
