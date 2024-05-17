package it.pagopa.ecommerce.commons.documents.v2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Data class that contains information about a transaction for which an error
 * occurred performing refund operation
 *
 * @see BaseTransactionRefundedData
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document
@AllArgsConstructor
@Generated
public final class TransactionRefundErrorData extends BaseTransactionRefundedData {

}
