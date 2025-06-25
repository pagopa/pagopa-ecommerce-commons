package it.pagopa.ecommerce.commons.documents.v2.deadletter;

import it.pagopa.ecommerce.commons.generated.npg.v1.dto.OperationResultDto;
import lombok.*;

import javax.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

/**
 * Transaction info for NPG gateway
 */
@Data
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

    /**
     * All-args constructor
     *
     * @param operationResult   the operation result
     * @param operationId       the operation id
     * @param correlationId     the correlation id
     * @param paymentEndToEndId the payment end to end id
     */
    public DeadLetterNpgTransactionInfoDetailsData(
            OperationResultDto operationResult,
            String operationId,
            String correlationId,
            String paymentEndToEndId
    ) {
        this.operationResult = operationResult;
        this.operationId = operationId;
        this.correlationId = correlationId;
        this.paymentEndToEndId = paymentEndToEndId;
    }

    @Override
    public TransactionInfoDataType getType() {
        return TYPE;
    }
}
