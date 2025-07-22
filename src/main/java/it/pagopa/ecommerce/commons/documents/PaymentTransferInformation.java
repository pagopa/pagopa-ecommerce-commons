package it.pagopa.ecommerce.commons.documents;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Base persistence view for notice code.
 */
@Data
@NoArgsConstructor
@Document
@Generated
public class PaymentTransferInformation {
    private String paFiscalCode;
    private Boolean digitalStamp;
    private Long transferAmount;
    private String transferCategory;

    /**
     * All-args constructor
     *
     * @param paFiscalCode     the PA fiscal code
     * @param digitalStamp     the digital stamp flag
     * @param transferAmount   the transfer amount
     * @param transferCategory the transfer category
     */
    public PaymentTransferInformation(
            String paFiscalCode,
            Boolean digitalStamp,
            Long transferAmount,
            String transferCategory
    ) {
        this.paFiscalCode = paFiscalCode;
        this.digitalStamp = digitalStamp;
        this.transferAmount = transferAmount;
        this.transferCategory = transferCategory;
    }
}
