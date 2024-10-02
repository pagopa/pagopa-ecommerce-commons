package it.pagopa.ecommerce.commons.documents;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/**
 * Persistence document for user statistics.
 */
@Data
@Document
@AllArgsConstructor
@NoArgsConstructor
@Generated
public class UserStatistics {

    @NotNull
    private String userId;

    @NotNull
    private UserStatistics.LastUsage lastUsage;

    /**
     * Persistence document for last usage of a payment method or wallet
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class LastUsage {
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
}
