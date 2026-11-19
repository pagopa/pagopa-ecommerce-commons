package it.pagopa.ecommerce.commons.documents.v2;

import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpStatus;

import javax.annotation.Nullable;

/**
 * Data class that contains information about a transaction for which an error
 * occurred performing refund operation
 *
 * @see BaseTransactionRefundedData
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document
@NoArgsConstructor
@Generated
public final class TransactionRefundErrorData extends BaseTransactionRefundedData {

    /**
     * Http error code received by Gateway in refund response: This field is null
     * when HTTP response error code cannot be detected (f.e. timeout)
     */
    @Nullable
    private HttpStatus httpErrorCode;

    /**
     * Gateway error description taken from error response body, if any
     */
    @Nullable
    private String errorDescription;

    /**
     * The error type, defaulted to COMMUNICATION_ERROR if not specified
     */
    @NotNull
    private ErrorType errorType = ErrorType.COMMUNICATION_ERROR;

    /**
     * Constructor
     *
     * @param statusBeforeRefunded the transaction before
     * @param httpErrorCode        gateway http error code performing refund: This
     *                             field is null when HTTP response error code
     *                             cannot be detected (f.e. timeout)
     * @param errorDescription     Gateway error description taken from error
     *                             response body, if any
     * @param errorType            the error type
     */
    public TransactionRefundErrorData(
            @NotNull TransactionStatusDto statusBeforeRefunded,
            @Nullable HttpStatus httpErrorCode,
            @Nullable String errorDescription,
            @Nullable ErrorType errorType
    ) {
        super(statusBeforeRefunded);
        this.httpErrorCode = httpErrorCode;
        this.errorDescription = errorDescription;
        if (errorType != null) {
            this.errorType = errorType;
        }
    }
}
