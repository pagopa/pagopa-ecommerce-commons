package it.pagopa.ecommerce.commons.documents.v2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpStatus;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Data related to closure error event
 */
@Data
@Document
@AllArgsConstructor
@NoArgsConstructor
@Generated
public class ClosureErrorData {

    /**
     * Enumeration of errors that can happen
     */
    public enum ErrorType {
        /**
         * KO response received from Node
         */
        KO_RESPONSE_RECEIVED,
        /**
         * Error happen during communication, no response have been received
         */
        COMMUNICATION_ERROR,
    }

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

}
