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
@Document
@AllArgsConstructor
@NoArgsConstructor
@Generated
public class NoticeCode {
    private String paymentToken;
    private String rptId; // TODO To be splitted
    private String description;
    private Integer amount;
}
