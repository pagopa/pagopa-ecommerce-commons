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
    private String authorizationRequestId;
    private PaymentGateway paymentGateway;
    private URI logo;
    @Nullable
    private String brand;

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
}
