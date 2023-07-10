package it.pagopa.ecommerce.commons.documents.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

import java.net.URI;

/**
 * Additional data when requesting a payment authorization
 */
@AllArgsConstructor
@Data
@Document
public class TransactionAuthorizationRequestData {

    private int amount;
    private int fee;
    private String paymentInstrumentId;
    private String pspId;
    private String paymentTypeCode;
    private String brokerName;
    private String pspChannelCode;
    private String paymentMethodName;
    private String pspBusinessName;
    private boolean isPspOnUs;
    private String authorizationRequestId;
    private PaymentGateway paymentGateway;
    private URI logo;
    @Nullable
    private CardBrand brand;

    /**
     * Enumeration of different PaymentGateway
     */
    public enum PaymentGateway {
        /**
         * VPOS payment gateway
         */
        VPOS,
        /**
         * XPAY payment gateway
         */
        XPAY,
        /**
         * POSTEPAY payment gateway
         */
        POSTEPAY
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
