package it.pagopa.ecommerce.commons.documents.v2.authorization;

import it.pagopa.ecommerce.commons.generated.server.model.AuthorizationResultDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.lang.Nullable;

/**
 * PGS transaction authorization completed data
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class PgsTransactionAuthorizationCompletedData implements TransactionAuthorizationCompletedData {

    /**
     * The PGS errorCode
     */
    @Nullable
    private String errorCode;

    /**
     * The payment gateway authorization outcome
     */
    private AuthorizationResultDto authorizationResultDto;
}
