package it.pagopa.ecommerce.commons.documents.v2.activation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Empty transaction activation data indicating no additional information are
 * available for the current transaction
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public final class EmptyTransactionGatewayActivationData implements TransactionGatewayActivationData {
}
