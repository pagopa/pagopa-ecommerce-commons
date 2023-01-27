package it.pagopa.ecommerce.commons.domain.pojos;

import it.pagopa.ecommerce.commons.documents.TransactionAuthorizationFailedEvent;
import it.pagopa.ecommerce.commons.documents.TransactionAuthorizationStatusUpdateData;
import it.pagopa.ecommerce.commons.generated.server.model.AuthorizationResultDto;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * <p>
 * POJO for a transaction with a failed authorization.
 * </p>
 *
 * @see BaseTransaction
 * @see it.pagopa.ecommerce.commons.documents.TransactionAuthorizationFailedEvent
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public abstract class BaseTransactionWithFailedAuthorization extends BaseTransactionWithCompletedAuthorization {
    private final TransactionAuthorizationFailedEvent event;

    /**
     * Primary constructor
     *
     * @param baseTransaction base transaction
     * @param event           failed authorization event
     */
    protected BaseTransactionWithFailedAuthorization(BaseTransactionWithRequestedAuthorization baseTransaction, TransactionAuthorizationFailedEvent event) {
        super(
                baseTransaction,
                new TransactionAuthorizationStatusUpdateData(
                        AuthorizationResultDto.KO,
                        TransactionStatusDto.AUTHORIZATION_FAILED
                )
        );

        this.event = event;
    }
}
