package it.pagopa.ecommerce.commons.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Base persistence view for notice code.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
@Generated
public class PaymentTransferInformation {
    private String paFiscalCode;
    private Boolean digitalStamp;
    private Integer transferAmount;
    private String transferCategory;
}
