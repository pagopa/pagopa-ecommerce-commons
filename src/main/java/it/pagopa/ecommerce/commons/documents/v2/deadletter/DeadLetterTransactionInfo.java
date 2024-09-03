package it.pagopa.ecommerce.commons.documents.v2.deadletter;

import it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationRequestData;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Transaction info for dead-letter data
 */
@AllArgsConstructor
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

    private String pspId;

    private String paymentMethodName;

    private Integer grandTotal;

    private String rrn;

    private DeadLetterTransactionInfoDetailsData details;

}
