package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.annotations.ValueObject;

@ValueObject
public record NoticeCode(PaymentToken paymentToken, RptId rptId, TransactionAmount transactionAmount, TransactionDescription transactionDescription) {
}
