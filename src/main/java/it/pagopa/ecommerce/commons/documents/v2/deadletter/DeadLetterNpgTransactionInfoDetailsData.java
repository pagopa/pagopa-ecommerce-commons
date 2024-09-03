package it.pagopa.ecommerce.commons.documents.v2.deadletter;

import it.pagopa.ecommerce.commons.generated.npg.v1.dto.OperationResultDto;
import lombok.*;

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
    private OperationResultDto operationResult;

    /**
     * NPG operation id
     */
    private String operationId;

    /**
     * NPG correlation id
     */
    private String correlationId;

    @NotNull
    private static final TransactionInfoDataType TYPE = TransactionInfoDataType.NPG;

    @Override
    public TransactionInfoDataType getType() {
        return TYPE;
    }
}
