package it.pagopa.ecommerce.commons.documents.v2;

import it.pagopa.ecommerce.commons.documents.PaymentNotice;
import it.pagopa.ecommerce.commons.documents.v2.activation.TransactionGatewayActivationData;
import it.pagopa.ecommerce.commons.domain.Confidential;
import it.pagopa.ecommerce.commons.domain.Email;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
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

}
