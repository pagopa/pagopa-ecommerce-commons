package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.annotations.ValueObject;

@ValueObject
public record TransactionAmount(int value) {
}
