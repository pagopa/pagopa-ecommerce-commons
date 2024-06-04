package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.annotations.ValueObject;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * <p>
 * A value object holding a jwt claims.
 * </p>
 *
 * @param transactionId   transactionId
 * @param orderId         orderId
 * @param paymentMethodId payment methodId
 * @param userId          the userId identified by the wallet token that
 *                        performs the payment
 */
@ValueObject
public record Claims(
        @Nullable TransactionId transactionId,
        @Nullable String orderId,
        @Nullable String paymentMethodId,
        @Nullable UUID userId
) {
}
