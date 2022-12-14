package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Data related to expiration event
 */
@Data
@Document
@AllArgsConstructor
@NoArgsConstructor
@Generated
public class TransactionExpiredData {

    private TransactionStatusDto statusBeforeExpiration;
}
