package it.pagopa.ecommerce.commons.documents.v2.info;

import it.pagopa.ecommerce.commons.generated.npg.v1.dto.OperationResultDto;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Transaction info for NPG gateway
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public final class NpgTransactionInfoDetailsData implements TransactionInfoDetailsData {

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
    private UUID correlationId;

    @NotNull
    private static final TransactionInfoDataType TYPE = TransactionInfoDataType.NPG;

    @Override
    public TransactionInfoDataType getType() {
        return TYPE;
    }
}
