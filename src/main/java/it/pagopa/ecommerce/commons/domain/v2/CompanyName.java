package it.pagopa.ecommerce.commons.domain.v2;

import it.pagopa.ecommerce.commons.annotations.ValueObject;

/**
 * <p>
 * Value object holding a company name.
 * </p>
 *
 * @param value the company name
 */
@ValueObject
public record CompanyName(String value) {
}
