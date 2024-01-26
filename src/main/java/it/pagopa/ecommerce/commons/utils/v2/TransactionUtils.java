package it.pagopa.ecommerce.commons.utils.v2;

import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransaction;
import it.pagopa.ecommerce.commons.domain.v2.pojos.BaseTransactionWithRequestedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * <p>
 * The utils class for transaction.
 * </p>
 */
@Component("TransactionUtilsV2")
public class TransactionUtils {

    /**
     * The method used for check if the specific status is transient
     *
     * @param status the status to check
     * @return boolean value true if the status is transient
     */
    public Boolean isTransientStatus(TransactionStatusDto status) {
        return TransactionStatusDto.ACTIVATED == status
                || TransactionStatusDto.AUTHORIZATION_REQUESTED == status
                || TransactionStatusDto.AUTHORIZATION_COMPLETED == status
                || TransactionStatusDto.CANCELLATION_REQUESTED == status
                || TransactionStatusDto.CLOSURE_REQUESTED == status
                || TransactionStatusDto.CLOSURE_ERROR == status
                || TransactionStatusDto.CLOSED == status
                || TransactionStatusDto.EXPIRED == status
                || TransactionStatusDto.NOTIFIED_KO == status
                || TransactionStatusDto.NOTIFICATION_ERROR == status
                || TransactionStatusDto.NOTIFICATION_REQUESTED == status;
    }

    /**
     * The method used for check if the specific status is refundable transaction
     *
     * @param status the status to check
     * @return boolean true if the status is equals to the list of refundable status
     */
    public Boolean isRefundableTransaction(TransactionStatusDto status) {
        return TransactionStatusDto.CLOSED == status
                || TransactionStatusDto.CLOSURE_REQUESTED == status
                || TransactionStatusDto.CLOSURE_ERROR == status
                || TransactionStatusDto.EXPIRED == status
                || TransactionStatusDto.AUTHORIZATION_COMPLETED == status
                || TransactionStatusDto.AUTHORIZATION_REQUESTED == status
                || TransactionStatusDto.NOTIFIED_KO == status
                || TransactionStatusDto.NOTIFICATION_ERROR == status;
    }

    /**
     * Gets a transaction fee from a generic transaction
     *
     * @param transaction the transaction
     * @return an {@link Optional} containing the transaction fee if the user
     *         requested an authorization, empty otherwise
     */
    public static Optional<Integer> getTransactionFee(BaseTransaction transaction) {
        if (transaction instanceof BaseTransactionWithRequestedAuthorization baseTransactionWithRequestedAuthorization) {
            return Optional
                    .of(baseTransactionWithRequestedAuthorization.getTransactionAuthorizationRequestData().getFee());
        } else {
            return Optional.empty();
        }
    }
}
