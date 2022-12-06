package it.pagopa.ecommerce.commons.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Data related to activation events
 */
@Data
@Document
@AllArgsConstructor
@NoArgsConstructor
@Generated
public class TransactionActivatedData {

    private String description;
    private Integer amount;
    private String email;
    private String faultCode; // TODO enum with all PAA & PTT
    private String faultCodeString;
    private String paymentToken;
}
