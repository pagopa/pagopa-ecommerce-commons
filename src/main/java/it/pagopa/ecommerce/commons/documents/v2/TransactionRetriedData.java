package it.pagopa.ecommerce.commons.documents.v2;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Data related to retry event
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document
@NoArgsConstructor
@Generated
public final class TransactionRetriedData extends BaseTransactionRetriedData {

    /**
     * Construtor
     *
     * @param retryCount the retry event counter
     */
    public TransactionRetriedData(Integer retryCount) {
        super(retryCount);
    }
}
