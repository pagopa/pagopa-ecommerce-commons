package it.pagopa.ecommerce.commons.documents.v2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Base class for a retry event
 *
 * @see TransactionRefundRetriedData
 * @see TransactionRetriedData
 */
@Data
@Document
@AllArgsConstructor
@NoArgsConstructor
@Generated
public sealed class BaseTransactionRetriedData permits TransactionClosureRetriedData,TransactionRefundRetriedData,TransactionRetriedData {
    /**
     * Retry event count
     */
    protected Integer retryCount;
}
