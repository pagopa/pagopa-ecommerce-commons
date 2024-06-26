package it.pagopa.ecommerce.commons.documents.v2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

/**
 * Data related to Nodo send payment result operation such as outcome (OK/KO)
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Document
public class TransactionUserReceiptData {

    /**
     * The Nodo sendPaymentResult outcome
     */
    @NotNull
    private Outcome responseOutcome;

    /**
     * Notification language
     */
    @NotNull
    private String language;

    /**
     * Payment date upon call from Nodo's `sendPaymentResult` (aka `PATCH
     * user-receipts` endpoint)
     */
    @NotNull
    private String paymentDate;

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
        KO,
        /**
         * sendPaymentResult not received yet
         */
        NOT_RECEIVED
    }
}
