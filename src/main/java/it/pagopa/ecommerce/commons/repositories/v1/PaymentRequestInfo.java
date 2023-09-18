package it.pagopa.ecommerce.commons.repositories.v1;

import it.pagopa.ecommerce.commons.domain.v1.IdempotencyKey;
import it.pagopa.ecommerce.commons.domain.v1.PaymentTransferInfo;
import it.pagopa.ecommerce.commons.domain.v1.RptId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Redis structure to hold volatile information about a payment request.
 *
 * @param id             RPT id of the request
 * @param paFiscalCode   Fiscal code of the public entity that published the
 *                       payment notice
 * @param paName         Human-readable name of the public entity that published
 *                       the payment notice
 * @param description    Description of the payment notice
 * @param amount         Amount on the payment notice
 * @param dueDate        Payment's due date
 * @param paymentToken   Payment token associated to this payment request
 * @param idempotencyKey Idempotency key associated to the payment request
 * @param transferList   Payment transfer list information
 * @param isAllCCP       isAllCCP flag for GEC. If true includes poste bundles,
 *                       false excludes them
 */
@RedisHash(value = "keys", timeToLive = 10 * 60)
public record PaymentRequestInfo(
        @NonNull @Id RptId id,
        @Nullable String paFiscalCode,
        @Nullable String paName,
        @Nullable String description,
        @Nullable Integer amount,
        @Nullable String dueDate,
        @Nullable String paymentToken,
        @Nullable String activationDate,
        @Nullable IdempotencyKey idempotencyKey,
        @Nullable List<PaymentTransferInfo> transferList,
        @Nullable Boolean isAllCCP
) {
    /**
     * Construct a {@link PaymentRequestInfo} from its components
     *
     * @param id             RPT id of the request
     * @param paFiscalCode   Fiscal code of the public entity that published the
     *                       payment notice
     * @param paName         Human-readable name of the public entity that published
     *                       the payment notice
     * @param description    Description of the payment notice
     * @param amount         Amount on the payment notice
     * @param dueDate        Payment's due date
     * @param paymentToken   Payment token associated to this payment request
     * @param activationDate Transaction activation date
     * @param idempotencyKey Idempotency key associated to the payment request
     * @param transferList   Payment transfer list information
     * @param isAllCCP       isAllCCP flag for GEC. If true includes poste bundles,
     *                       false excludes them
     */
    /*
     * @formatter:off
     *
     * Warning java:S6207 - Default constructor used here as placeholder for
     * @PersistenceConstructor annotation
     *
     * @formatter:on
     */
    @SuppressWarnings("java:S6207")
    @PersistenceConstructor
    public PaymentRequestInfo {
        // Do nothing
    }
}
