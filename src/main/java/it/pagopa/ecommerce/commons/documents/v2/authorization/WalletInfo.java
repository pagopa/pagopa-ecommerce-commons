package it.pagopa.ecommerce.commons.documents.v2.authorization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * This class contains details about the wallet used to perform authorization
 * request
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WalletInfo {

    @NotNull
    private String walletId;

    @Nullable
    private WalletDetails walletDetails;

    /**
     * Common interface for all wallet details
     */

    @JsonIgnoreProperties(
            value = "type", allowSetters = true
    )
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes(
        {
                @JsonSubTypes.Type(value = CardWalletDetails.class, name = "CARDS"),
                @JsonSubTypes.Type(value = PaypalWalletDetails.class, name = "PAYPAL")
        }
    )
    public interface WalletDetails {
        /**
         * Wallet type enumeration
         */
        enum WalletType {
            /**
             * Cards wallet
             */
            CARDS,
            /**
             * Paypal wallet
             */
            PAYPAL
        }

        /**
         * Get the wallet type
         *
         * @return this wallet type
         */
        WalletType getType();
    }

    /**
     * Card wallet details
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class CardWalletDetails implements WalletDetails {
        @NotNull
        private final WalletType type = WalletType.CARDS;

        @NotNull
        private String bin;
        @NotNull
        private String lastFourDigits;
    }

    /**
     * Paypal wallet details
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class PaypalWalletDetails implements WalletDetails {
        @NotNull
        private final WalletType type = WalletType.PAYPAL;

        @NotNull
        private String maskedEmail;
    }

}
