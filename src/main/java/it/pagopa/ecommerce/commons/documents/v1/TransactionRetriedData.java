package it.pagopa.ecommerce.commons.documents.v1;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Data related to retry event
 */
@Data
@Document
@NoArgsConstructor
@Generated
public class TransactionRetriedData {

    private Integer retryCount;

    /**
     * All-args constructor
     *
     * @param retryCount the retry attempt count
     */
    public TransactionRetriedData(Integer retryCount) {
        this.retryCount = retryCount;
    }
}
