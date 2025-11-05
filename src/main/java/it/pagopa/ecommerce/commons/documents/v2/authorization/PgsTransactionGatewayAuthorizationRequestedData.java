package it.pagopa.ecommerce.commons.documents.v2.authorization;

import lombok.*;
import org.springframework.lang.Nullable;

import jakarta.validation.constraints.NotNull;
import java.net.URI;

/**
 * PGS transaction authorization requested data
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ToString(callSuper = true)
public final class PgsTransactionGatewayAuthorizationRequestedData
        implements TransactionGatewayAuthorizationRequestedData {

    private URI logo;
    @Nullable
    private CardBrand brand;

    @NotNull
    private static final TransactionGatewayAuthorizationRequestedData.AuthorizationDataType TYPE = TransactionGatewayAuthorizationRequestedData.AuthorizationDataType.PGS;

    /**
     * All-args constructor
     *
     * @param logo  the logo URI
     * @param brand the card brand
     */
    public PgsTransactionGatewayAuthorizationRequestedData(
            URI logo,
            CardBrand brand
    ) {
        this.logo = logo;
        this.brand = brand;
    }

    @Override
    public AuthorizationDataType getType() {
        return TYPE;
    }

    /**
     * Enumeration of different brand type
     */
    public enum CardBrand {
        /**
         * brand type VISA
         */
        VISA,
        /**
         * brand type MASTERCARD
         */
        MASTERCARD,
        /**
         * brand type UNKNOWN
         */
        UNKNOWN,
        /**
         * brand type DINERS
         */
        DINERS,
        /**
         * brand type MAESTRO
         */
        MAESTRO,
        /**
         * brand type AMEX
         */
        AMEX;
    }
}
