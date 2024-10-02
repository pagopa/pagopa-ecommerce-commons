package it.pagopa.ecommerce.commons.documents.userstats.v1;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/**
 * Persistence document for last usage of a payment method or wallet
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LastUsage {
    /**
     * Payment type enumeration
     */
    public enum PaymentType {
        /**
         * User wallet
         */
        SAVED_WALLET,
        /**
         * Guest payment method
         */
        GUEST_PAYMENT_METHOD
    }

    /**
     * The payment type, saved_wallet or guest_payment_method
     */
    @NotNull
    private LastUsage.PaymentType type;

    /**
     * The id of the method used to pay
     */
    @NotNull
    private UUID instrumentId;

    /**
     * The date of the last usage
     */
    @NotNull
    private Instant date;
}
