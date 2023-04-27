package it.pagopa.ecommerce.commons.documents.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.net.URI;

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
     * Notification payment method logo URI
     */
    @NotNull
    private URI paymentMethodLogoUri;

    /**
     * Payment date upon call from Nodo's `sendPaymentResult` (aka `PATCH
     * user-receipts` endpoint)
     */
    @NotNull
    private String paymentDate;
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
     * RRN information for vpos payment
     */
    private String rrn;

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
