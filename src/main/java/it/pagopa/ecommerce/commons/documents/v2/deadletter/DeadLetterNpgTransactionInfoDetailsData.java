package it.pagopa.ecommerce.commons.documents.v2.deadletter;

import it.pagopa.ecommerce.commons.generated.npg.v1.dto.OperationResultDto;
import lombok.*;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Transaction info for NPG gateway
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public final class DeadLetterNpgTransactionInfoDetailsData implements DeadLetterTransactionInfoDetailsData {

    /**
     * NPG operation result
     */
    @Nullable
    private OperationResultDto operationResult;

    /**
     * NPG operation id
     */
    @Nullable
    private String operationId;

    /**
     * NPG correlation id
     */
    @Nullable
    private String correlationId;

    /**
     * NPG payment end to end id
     */
    @Nullable
    private String paymentEndToEndId;

    @NotNull
    private static final TransactionInfoDataType TYPE = TransactionInfoDataType.NPG;

    @Override
    public TransactionInfoDataType getType() {
        return TYPE;
    }
}
