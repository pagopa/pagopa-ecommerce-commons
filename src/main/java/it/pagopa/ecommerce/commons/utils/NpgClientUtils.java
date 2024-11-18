package it.pagopa.ecommerce.commons.utils;

import it.pagopa.ecommerce.commons.client.NpgClient;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.OperationDto;

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
    }

    /**
     * Retrieves the `paymentEndToEndId` value for the specified payment circuit
     * from the NPG `getOrder` response.
     *
     * @param operationDto The response object from the NPG `getOrder` call. This
     *                     object contains details about the payment operation,
     *                     including the payment circuit and additional data.
     * @return The `paymentEndToEndId` corresponding to the payment circuit, or
     *         {@code null} if the value cannot be determined.
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
        if (operationDto == null || operationDto.getPaymentCircuit() == null)
            return null;

        if (operationDto.getPaymentCircuit().equals(NpgClient.PaymentMethod.BANCOMATPAY.serviceName)) {
            // for bancomatPay we expect an `bpayEndToEndId` entry into additional data map
            // to be used as
            // the paymentEndToEndId
            return operationDto.getAdditionalData() == null ? operationDto.getPaymentEndToEndId()
                    : (String) operationDto.getAdditionalData().get(EndToEndId.BANCOMAT_PAY.value);
        } else if (operationDto.getPaymentCircuit().equals(NpgClient.PaymentMethod.MYBANK.serviceName)) {
            return operationDto.getAdditionalData() == null ? operationDto.getPaymentEndToEndId()
                    : (String) operationDto.getAdditionalData().get(EndToEndId.MYBANK.value);
        } else {
            return operationDto.getPaymentEndToEndId();
        }
    }
}
