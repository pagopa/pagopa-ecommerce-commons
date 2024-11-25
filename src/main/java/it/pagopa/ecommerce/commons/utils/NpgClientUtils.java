package it.pagopa.ecommerce.commons.utils;

import it.pagopa.ecommerce.commons.client.NpgClient;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.OperationDto;

import java.util.Optional;

/**
 * Utility class for handling NPG client operations.
 */
public class NpgClientUtils {

    /**
     * Create NpgClientUtils object.
     */
    private NpgClientUtils() {
        throw new IllegalStateException("Utility EuroUtils class");
    }

    /**
     * Enum representing the mapping between payment circuits and their respective
     * field names for the `paymentEndToEndId` in the additional data map.
     */
    public enum EndToEndId {
        /**
         * Represents the Bancomat Pay circuit. The corresponding field name in the
         * additional data map is `bpayEndToEndId`.
         */
        BANCOMAT_PAY("bpayEndToEndId"),
        /**
         * Represents the MyBank circuit. The corresponding field name in the additional
         * data map is `myBankEndToEndId`.
         */
        MYBANK("myBankEndToEndId");

        /**
         * The field name in the additional data map associated with this payment
         * circuit.
         */
        public final String value;

        /**
         * Constructor for the EndToEndId enum.
         *
         * @param value The field name in the additional data map corresponding to the
         *              payment circuit.
         */
        EndToEndId(String value) {
            this.value = value;
        }

        /**
         * Retrieves the field name in the additional data map associated with the
         * payment circuit represented by this enum instance.
         *
         * @return the field name in the additional data map corresponding to the
         *         payment circuit.
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * Retrieves the `paymentEndToEndId` value for the specified payment circuit
     * from the NPG `getOrder` response.
     *
     * @param operationDto The response object from the NPG `getOrder` call. This
     *                     object contains details about the payment operation,
     *                     including the payment circuit and additional data.
     * @return The `paymentEndToEndId` corresponding to the payment circuit, or
     *         {@code null} if the operationDto is null.
     *
     *         <p>
     *         For supported payment circuits:
     *         <ul>
     *         <li>If the circuit is Bancomat Pay, the value of `bpayEndToEndId`
     *         from the additional data map is returned.</li>
     *         <li>If the circuit is MyBank, the value of `myBankEndToEndId` from
     *         the additional data map is returned.</li>
     *         </ul>
     *         If the circuit is unsupported or additional data is unavailable, the
     *         `paymentEndToEndId` field from the `operationDto` object is used as a
     *         fallback.
     */
    public static String getPaymentEndToEndId(OperationDto operationDto) {
        String paymentEndToEndId = Optional.ofNullable(operationDto).map(OperationDto::getPaymentEndToEndId)
                .orElse(null);
        return Optional
                .ofNullable(operationDto)
                .filter(op -> op.getPaymentCircuit() != null)
                .flatMap(
                        op -> Optional.ofNullable(op.getAdditionalData())
                )
                .map(
                        additionalData -> switch (NpgClient.PaymentMethod
                                .fromServiceName(operationDto.getPaymentCircuit())) {
                        case BANCOMATPAY -> additionalData.get(EndToEndId.BANCOMAT_PAY.getValue()).toString();
                        case MYBANK -> additionalData.get(EndToEndId.MYBANK.getValue()).toString();
                        default -> paymentEndToEndId;
                        }
                )
                .orElse(paymentEndToEndId);
    }
}
