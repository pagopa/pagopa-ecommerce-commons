package it.pagopa.ecommerce.commons.documents.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
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
     * Specifies if the notification was initiated manually or triggered
     * automatically. A manual notification typically occurs when an operator
     * explicitly initiates a notification operation. An automatic notification is
     * requested directly by the system flow in response to specific conditions.
     */
    @Null
    private NotificationTrigger notificationTrigger;

    /**
     * Constructor for creating a TransactionUserReceiptData with basic parameters
     *
     * @param responseOutcome The outcome of the payment response
     * @param language        The notification language
     * @param paymentDate     The date of the payment
     */
    public TransactionUserReceiptData(
            Outcome responseOutcome,
            String language,
            String paymentDate
    ) {
        this.responseOutcome = responseOutcome;
        this.language = language;
        this.paymentDate = paymentDate;
    }

    /**
     * Constructor for creating a TransactionUserReceiptData with all parameters
     * including notification trigger
     *
     * @param responseOutcome     The outcome of the payment response
     * @param language            The notification language
     * @param paymentDate         The date of the payment
     * @param notificationTrigger The trigger type for the notification (AUTOMATIC
     *                            or MANUAL)
     */
    public TransactionUserReceiptData(
            Outcome responseOutcome,
            String language,
            String paymentDate,
            NotificationTrigger notificationTrigger
    ) {
        this.responseOutcome = responseOutcome;
        this.language = language;
        this.paymentDate = paymentDate;
        this.notificationTrigger = notificationTrigger;
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

    /**
     * Enumeration of possible notification operation trigger
     */
    public enum NotificationTrigger {
        /**
         * requested directly by the system flow in response to specific conditions
         */
        AUTOMATIC,
        /**
         * an operator explicitly initiates a notification operation
         */
        MANUAL
    }
}
