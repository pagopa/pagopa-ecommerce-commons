package it.pagopa.ecommerce.commons.documents.v1;

import it.pagopa.ecommerce.commons.domain.Confidential;
import it.pagopa.ecommerce.commons.domain.v1.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private String faultCode; // TODO enum with all PAA & PTT
    private String faultCodeString;
    private Transaction.ClientId clientId;

}
