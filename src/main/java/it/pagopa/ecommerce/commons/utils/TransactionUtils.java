package it.pagopa.ecommerce.commons.utils;

import it.pagopa.ecommerce.commons.domain.pojos.BaseTransaction;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithRequestedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * <p>
 * The utils class for transaction.
 * </p>
 */
@Component
public class TransactionUtils {

    /**
     * The method used for check if the specific status is transient
     *
     * @param status the status to check
     * @return boolean value true if the status is transient
     */
    public Boolean isTransientStatus(TransactionStatusDto status) {
        return TransactionStatusDto.ACTIVATED == status
                || TransactionStatusDto.AUTHORIZED == status
                || TransactionStatusDto.AUTHORIZATION_REQUESTED == status
                || TransactionStatusDto.AUTHORIZATION_FAILED == status
                || TransactionStatusDto.CLOSURE_FAILED == status
                || TransactionStatusDto.CLOSED == status;
    }

    /**
     * The method used for check if the specific status is refundable transaction
     *
     * @param status the status to check
     * @return boolean true if the status is equals to the list of refundable status
     */
    public Boolean isRefundableTransaction(TransactionStatusDto status) {
        return TransactionStatusDto.AUTHORIZED == status
                || TransactionStatusDto.AUTHORIZATION_REQUESTED == status
                || TransactionStatusDto.AUTHORIZATION_FAILED == status
                || TransactionStatusDto.CLOSURE_FAILED == status;
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
