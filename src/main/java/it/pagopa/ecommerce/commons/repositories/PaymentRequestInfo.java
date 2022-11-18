package it.pagopa.ecommerce.commons.repositories;

import it.pagopa.ecommerce.commons.domain.IdempotencyKey;
import it.pagopa.ecommerce.commons.domain.RptId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.constraints.Pattern;

/**
 * <p>Redis structure to hold volatile information about a payment request.</p>
 *
 * @param id             RPT id of the request
 * @param paFiscalCode   Fiscal code of the public entity that published the payment notice
 * @param paName         Human-readable name of the public entity that published the payment notice
 * @param description    Description of the payment notice
 * @param amount         Amount on the payment notice
 * @param dueDate        Payment's due date
 * @param isNM3          If true, this payment notice must go through the new payment flow (Nuovo Modello 3) instead of the legacy one.
 * @param paymentToken   Payment token associated to this payment request. May be null iff {@code isNM3} is {@code false}
 * @param idempotencyKey Idempotency key associated to the payment request
 */
@RedisHash(value = "keys", timeToLive = 10 * 60)
public record PaymentRequestInfo(@NonNull @Id RptId id,
                                 @NonNull String paFiscalCode,
                                 @NonNull String paName,
                                 @NonNull String description,
                                 @NonNull Integer amount,
                                 @NonNull @Pattern(regexp = "([a-zA-Z\\d]{1,35})|(RF\\d{2}[a-zA-Z\\d]{1,21})") String dueDate,
                                 @NonNull Boolean isNM3,
                                 @Nullable String paymentToken,
                                 @NonNull IdempotencyKey idempotencyKey) {
    @PersistenceConstructor
    public PaymentRequestInfo {
        // Do nothing
    }
}
