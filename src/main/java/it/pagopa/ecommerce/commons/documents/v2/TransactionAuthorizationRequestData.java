package it.pagopa.ecommerce.commons.documents.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import it.pagopa.ecommerce.commons.documents.v2.authorization.TransactionGatewayAuthorizationRequestedData;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

/**
 * Additional data when requesting a payment authorization
 */
@AllArgsConstructor
@Data
@Document
public class TransactionAuthorizationRequestData {

    private long amount;
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
    private String paymentMethodDescription;
    @NotNull
    private TransactionGatewayAuthorizationRequestedData transactionGatewayAuthorizationRequestedData;

    /**
     * Bundle unique id, set as nullable for backward compatibility with previously
     * written events
     */
    @Nullable
    private String idBundle;

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
         * NPG payment gateway
         */
        NPG,

        /**
         * Redirect payment gateway
         */
        REDIRECT
    }

    @JsonCreator
    private TransactionAuthorizationRequestData() {
    }
}
