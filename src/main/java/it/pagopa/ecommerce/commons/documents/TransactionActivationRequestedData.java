package it.pagopa.ecommerce.commons.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Data related to the requesting of an activation.
 */
@Data
@Document
@AllArgsConstructor
@NoArgsConstructor
@Generated
public class TransactionActivationRequestedData {

    // TODO Add other fields?
    private List<PaymentNotice> paymentNotices;
    private String email;
    private String faultCode; // TODO enum with all PAA & PTT
    private String faultCodeString;
}
