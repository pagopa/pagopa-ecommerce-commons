package it.pagopa.ecommerce.commons.documents;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Base transaction view version agnostic class.
 */
@Data
@NoArgsConstructor
@Document(collection = "transactions-view")
public abstract class BaseTransactionView {
    @Id
    private String transactionId;

    /**
     * All-args constructor
     *
     * @param transactionId the transaction id
     */
    protected BaseTransactionView(String transactionId) {
        this.transactionId = transactionId;
    }
}
