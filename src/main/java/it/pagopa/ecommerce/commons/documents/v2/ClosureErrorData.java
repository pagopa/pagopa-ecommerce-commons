package it.pagopa.ecommerce.commons.documents.v2;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpStatus;

import javax.annotation.Nullable;

/**
 * Data related to closure error event
 */
@Data
@Document
@NoArgsConstructor
@Generated
public class ClosureErrorData {

    /**
     * Http error code received by Node in close payment response: This field is
     * null when HTTP response error code cannot be detected (f.e. timeout)
     */
    @Nullable
    private HttpStatus httpErrorCode;

    /**
     * Node error description taken from error response body, if any
     */
    @Nullable
    private String errorDescription;

    @NotNull
    private ErrorType errorType;

    /**
     * All-args constructor
     *
     * @param httpErrorCode    the http error code received
     * @param errorDescription the error description received
     * @param errorType        the type of error
     */
    public ClosureErrorData(
            HttpStatus httpErrorCode,
            String errorDescription,
            ErrorType errorType
    ) {
        this.httpErrorCode = httpErrorCode;
        this.errorDescription = errorDescription;
        this.errorType = errorType;
    }
}
