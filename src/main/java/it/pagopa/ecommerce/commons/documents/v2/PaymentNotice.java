package it.pagopa.ecommerce.commons.documents.v2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Base persistence view for notice code.
 */
@Data
@Document
@AllArgsConstructor
@NoArgsConstructor
@Generated
public class PaymentNotice {
    private String paymentToken;
    private String rptId;
    private String description;
    private Integer amount;
    private String paymentContextCode;
    private List<PaymentTransferInformation> transferList;
    private boolean isAllCCP;
}