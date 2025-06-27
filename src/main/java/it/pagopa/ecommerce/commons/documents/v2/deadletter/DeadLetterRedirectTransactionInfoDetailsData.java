package it.pagopa.ecommerce.commons.documents.v2.deadletter;

import lombok.*;

import javax.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

/**
 * Transaction info for Redirect gateway
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public final class DeadLetterRedirectTransactionInfoDetailsData implements DeadLetterTransactionInfoDetailsData {

    /**
     * Redirect outcome
     */
    @Nullable
    private String outcome;

    @NotNull
    private static final TransactionInfoDataType TYPE = TransactionInfoDataType.REDIRECT;

    /**
     * All-args constructor
     *
     * @param outcome the redirect outcome
     */
    public DeadLetterRedirectTransactionInfoDetailsData(String outcome) {
        this.outcome = outcome;
    }

    @Override
    public TransactionInfoDataType getType() {
        return TYPE;
    }
}
