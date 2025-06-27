package it.pagopa.ecommerce.commons.documents.v2;

import it.pagopa.ecommerce.commons.documents.PaymentNotice;
import it.pagopa.ecommerce.commons.documents.v2.activation.TransactionGatewayActivationData;
import it.pagopa.ecommerce.commons.domain.Confidential;
import it.pagopa.ecommerce.commons.domain.v2.Email;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Data related to activation events
 */
@Data
@Document
@NoArgsConstructor
@Generated
public class TransactionActivatedData {
    private Confidential<Email> email;
    private List<PaymentNotice> paymentNotices;
    private String faultCode;
    private String faultCodeString;
    private Transaction.ClientId clientId;
    @Nullable
    private String idCart;
    private int paymentTokenValiditySeconds;
    private TransactionGatewayActivationData transactionGatewayActivationData;
    @Nullable
    private String userId;

    /**
     * All-args constructor
     *
     * @param email                            the user email
     * @param paymentNotices                   the list of payment notices
     * @param faultCode                        the fault code
     * @param faultCodeString                  the fault code string description
     * @param clientId                         the client id
     * @param idCart                           the cart id
     * @param paymentTokenValiditySeconds      the payment token validity in seconds
     * @param transactionGatewayActivationData the transaction gateway activation
     *                                         data
     * @param userId                           the user id
     */
    @SuppressWarnings("java:S107") // Constructor has 9 parameters, required for activation data model
    public TransactionActivatedData(
            Confidential<Email> email,
            List<PaymentNotice> paymentNotices,
            String faultCode,
            String faultCodeString,
            Transaction.ClientId clientId,
            @Nullable String idCart,
            int paymentTokenValiditySeconds,
            TransactionGatewayActivationData transactionGatewayActivationData,
            @Nullable String userId
    ) {
        this.email = email;
        this.paymentNotices = paymentNotices;
        this.faultCode = faultCode;
        this.faultCodeString = faultCodeString;
        this.clientId = clientId;
        this.idCart = idCart;
        this.paymentTokenValiditySeconds = paymentTokenValiditySeconds;
        this.transactionGatewayActivationData = transactionGatewayActivationData;
        this.userId = userId;
    }
}
