package it.pagopa.ecommerce.commons.documents.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

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
     * Notification language
     */
    @NotNull
    private String language;
    /**
     * Notification payment method logo
     */
    @NotNull
    private String paymentMethodLogo;

    /**
     * Payment date upon call from Nodo's `sendPaymentResult` (aka `PATCH user-receipts` endpoint)
     */
    @NotNull
    private OffsetDateTime paymentDate;
    /**
     * Send payment result receiving office name
     */
    @NotNull
    private String receivingOfficeName;
    /**
     * Send payment result payment description
     */
    @NotNull
    private String paymentDescription;

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
