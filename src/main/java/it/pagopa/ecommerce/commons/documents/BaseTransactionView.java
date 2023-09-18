package it.pagopa.ecommerce.commons.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Base transaction view version agnostic class.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "transactions-view")
public abstract class BaseTransactionView {
    @Id
    private String transactionId;
}
