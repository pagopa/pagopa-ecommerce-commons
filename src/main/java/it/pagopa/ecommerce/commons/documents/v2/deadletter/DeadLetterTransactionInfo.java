package it.pagopa.ecommerce.commons.documents.v2.deadletter;

import it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationRequestData;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Transaction info for dead-letter data
 */
@NoArgsConstructor
@Data
@Document
public class DeadLetterTransactionInfo {

    @NotNull
    private String transactionId;

    @NotNull
    private String authorizationRequestId;

    @NotNull
    private TransactionStatusDto eCommerceStatus;

    @NotNull
    private TransactionAuthorizationRequestData.PaymentGateway gateway;

    @NotNull
    private List<String> paymentTokens;

    @Nullable
    private String pspId;

    @Nullable
    private String paymentMethodName;

    @Nullable
    private Integer grandTotal;

    @Nullable
    private String rrn;

    @Nullable
    private DeadLetterTransactionInfoDetailsData details;

    /**
     * All-args constructor
     *
     * @param transactionId          the transaction id
     * @param authorizationRequestId the authorization request id
     * @param eCommerceStatus        the eCommerce transaction status
     * @param gateway                the payment gateway
     * @param paymentTokens          the list of payment tokens
     * @param pspId                  the PSP id
     * @param paymentMethodName      the payment method name
     * @param grandTotal             the transaction grand total
     * @param rrn                    the retrieval reference number
     * @param details                the gateway-specific details
     */
    @SuppressWarnings("java:S107") // Constructor has 10 parameters, required for dead letter info model
    public DeadLetterTransactionInfo(
            String transactionId,
            String authorizationRequestId,
            TransactionStatusDto eCommerceStatus,
            TransactionAuthorizationRequestData.PaymentGateway gateway,
            List<String> paymentTokens,
            String pspId,
            String paymentMethodName,
            Integer grandTotal,
            String rrn,
            DeadLetterTransactionInfoDetailsData details
    ) {
        this.transactionId = transactionId;
        this.authorizationRequestId = authorizationRequestId;
        this.eCommerceStatus = eCommerceStatus;
        this.gateway = gateway;
        this.paymentTokens = paymentTokens;
        this.pspId = pspId;
        this.paymentMethodName = paymentMethodName;
        this.grandTotal = grandTotal;
        this.rrn = rrn;
        this.details = details;
    }
}
