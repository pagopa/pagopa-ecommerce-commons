package it.pagopa.ecommerce.commons.documents.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

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

    /**
     * Enum class to handle different payment gateways
     **/
    public enum PaymentGateway {
        VPOS("VPOS"),
        XPAY("XPAY"),
        POSTEPAY("POSTEPAY");

        public final String value;

        PaymentGateway(String paymentGateway) {
            this.value = paymentGateway;
        }
    }
}
