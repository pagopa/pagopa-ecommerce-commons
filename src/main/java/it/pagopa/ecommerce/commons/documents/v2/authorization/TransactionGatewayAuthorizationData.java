package it.pagopa.ecommerce.commons.documents.v2.authorization;

/**
 * Extensible interface to handle multiple payment gateway transaction outcome
 */
public sealed interface TransactionGatewayAuthorizationData permits NpgTransactionGatewayAuthorizationData,PgsTransactionGatewayAuthorizationData {
}
