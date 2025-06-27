package it.pagopa.ecommerce.commons.documents.v1;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;

/**
 * Data related to Nodo send payment result operation such as outcome (OK/KO)
 */
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
     * Send payment result receiving office name
     */
    private String receivingOfficeName;
    /**
     * Send payment result payment description
     */
    @NotNull
    private String paymentDescription;

    /**
     * All-args constructor
     *
     * @param responseOutcome     the response outcome
     * @param language            the language
     * @param paymentDate         the payment date
     * @param receivingOfficeName the receiving office name
     * @param paymentDescription  the payment description
     */
    public TransactionUserReceiptData(
            Outcome responseOutcome,
            String language,
            String paymentDate,
            String receivingOfficeName,
            String paymentDescription
    ) {
        this.responseOutcome = responseOutcome;
        this.language = language;
        this.paymentDate = paymentDate;
        this.receivingOfficeName = receivingOfficeName;
        this.paymentDescription = paymentDescription;
    }

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
