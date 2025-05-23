package it.pagopa.ecommerce.commons.documents.v2.deadletter;

import it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationRequestData;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
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

}
