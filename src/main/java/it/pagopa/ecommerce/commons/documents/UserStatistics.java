package it.pagopa.ecommerce.commons.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.UUID;


/**
 * Base persistence view for user statistics.
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
    private LastMethodUsed lastMethodUsed;

    @JsonIgnoreProperties(
            value = "type", allowSetters = true
    )
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes(
            {
                    @JsonSubTypes.Type(value = WalletPayment.class, name = "CARDS"),
                    @JsonSubTypes.Type(value = GuestPayment.class, name = "PAYPAL")
            }
    )
    public interface LastMethodUsed {
        /**
         * Wallet type enumeration
         */
        enum PaymentType {
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
         * Get the wallet type
         *
         * @return this wallet type
         */
        UserStatistics.LastMethodUsed.PaymentType getType();
    }

    /**
     * Card wallet details
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class WalletPayment implements UserStatistics.LastMethodUsed {
        @NotNull
        private final PaymentType type = PaymentType.SAVED_WALLET;

        @NotNull
        private UUID walletId;
    }

    /**
     * Paypal wallet details
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class GuestPayment implements UserStatistics.LastMethodUsed {
        @NotNull
        private final PaymentType type = PaymentType.GUEST_PAYMENT_METHOD;

        @NotNull
        private UUID paymentMethodId;
    }

}
