package it.pagopa.ecommerce.commons.documents.v2;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nullable;

/**
 * Data related to retry event for a transaction closure operation
 *
 * @see BaseTransactionRetriedData
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Document
@NoArgsConstructor
@Generated
public final class TransactionClosureRetriedData extends BaseTransactionRetriedData {

    @Nullable
    private ClosureErrorData closureErrorData;

    /**
     * Constructor
     *
     * @param closureErrorData node closure error data
     * @param retryCount       the retry event counter
     */
    public TransactionClosureRetriedData(
            @Nullable ClosureErrorData closureErrorData,
            Integer retryCount
    ) {
        super(retryCount);
        this.closureErrorData = closureErrorData;
    }
}
