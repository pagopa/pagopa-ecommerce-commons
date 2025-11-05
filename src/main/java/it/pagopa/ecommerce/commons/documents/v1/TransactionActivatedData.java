package it.pagopa.ecommerce.commons.documents.v1;

import it.pagopa.ecommerce.commons.documents.PaymentNotice;
import it.pagopa.ecommerce.commons.domain.Confidential;
import it.pagopa.ecommerce.commons.domain.v1.Email;
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
    private String faultCode; // TODO enum with all PAA & PTT
    private String faultCodeString;
    private Transaction.ClientId clientId;
    @Nullable
    private String idCart;
    private int paymentTokenValiditySeconds;

    /**
     * All-args constructor
     *
     * @param email                       the user email
     * @param paymentNotices              the list of payment notices
     * @param faultCode                   the fault code
     * @param faultCodeString             the fault code string description
     * @param clientId                    the client id
     * @param idCart                      the cart id
     * @param paymentTokenValiditySeconds the payment token validity in seconds
     */
    public TransactionActivatedData(
            Confidential<Email> email,
            List<PaymentNotice> paymentNotices,
            String faultCode,
            String faultCodeString,
            Transaction.ClientId clientId,
            @Nullable String idCart,
            int paymentTokenValiditySeconds
    ) {
        this.email = email;
        this.paymentNotices = paymentNotices;
        this.faultCode = faultCode;
        this.faultCodeString = faultCodeString;
        this.clientId = clientId;
        this.idCart = idCart;
        this.paymentTokenValiditySeconds = paymentTokenValiditySeconds;
    }

}
