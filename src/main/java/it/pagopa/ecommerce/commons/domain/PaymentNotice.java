package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.annotations.ValueObject;

import java.util.List;

/**
 * <p>
 * A value object holding a notice code params.
 * </p>
 *
 * @param paymentToken           notice payment
 * @param transactionDescription notice description
 * @param transactionAmount      notice amount
 * @param rptId                  notice code rptId
 */
@ValueObject
public record PaymentNotice(
        PaymentToken paymentToken,
        RptId rptId,
        TransactionAmount transactionAmount,
        TransactionDescription transactionDescription,
        PaymentContextCode paymentContextCode,

        List<PaymentTransferInfo> transferList,

        boolean isAllCCP
) {
}
