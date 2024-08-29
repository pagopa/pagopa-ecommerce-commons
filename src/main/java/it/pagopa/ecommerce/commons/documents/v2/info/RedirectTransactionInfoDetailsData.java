package it.pagopa.ecommerce.commons.documents.v2.info;

import lombok.*;

import javax.validation.constraints.NotNull;

/**
 * Transaction info for Redirect gateway
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public final class RedirectTransactionInfoDetailsData implements TransactionInfoDetailsData {

    /**
     * Redirect outcome
     */
    private String outcome;

    @NotNull
    private static final TransactionInfoDataType TYPE = TransactionInfoDataType.REDIRECT;

    @Override
    public TransactionInfoDataType getType() {
        return TYPE;
    }
}
