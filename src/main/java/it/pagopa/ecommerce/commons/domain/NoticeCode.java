package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.annotations.ValueObject;

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
public record NoticeCode(
        PaymentToken paymentToken,
        RptId rptId,
        TransactionAmount transactionAmount,
        TransactionDescription transactionDescription
) {
}
