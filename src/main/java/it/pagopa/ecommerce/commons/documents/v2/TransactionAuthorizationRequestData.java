package it.pagopa.ecommerce.commons.documents.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import it.pagopa.ecommerce.commons.documents.v2.authorization.TransactionGatewayAuthorizationRequestedData;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

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
    private String paymentMethodDescription;
    @NotNull
    private TransactionGatewayAuthorizationRequestedData transactionGatewayAuthorizationRequestedData;

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
        POSTEPAY,
        /**
         * NPG payment gateway
         */
        NPG
    }

    @JsonCreator
    private TransactionAuthorizationRequestData() {
    }
}
