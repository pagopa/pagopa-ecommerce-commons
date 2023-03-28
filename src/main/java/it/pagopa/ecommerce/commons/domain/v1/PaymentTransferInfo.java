package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.annotations.ValueObject;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Structure to identify the beneficiary of a payment.
 *
 * @param paFiscalCode     Fiscal code of the beneficiary entity
 * @param digitalStamp     boolean identifies the presence of the stamp
 * @param transferCategory transfer category information
 * @param transferAmount   transfer amount
 */
@ValueObject
public record PaymentTransferInfo(
        @NonNull String paFiscalCode,
        @NonNull Boolean digitalStamp,
        @Nullable String transferCategory,
        @NonNull Integer transferAmount

) {
    /**
     * Structure to identify the beneficiary of a payment.
     *
     * @param paFiscalCode     Fiscal code of the beneficiary entity
     * @param digitalStamp     boolean identifies the presence of the stamp
     * @param transferCategory transfer category information
     * @param transferAmount   transfer amount
     */
    @PersistenceConstructor
    public PaymentTransferInfo {
        // Do nothing
    }
}
