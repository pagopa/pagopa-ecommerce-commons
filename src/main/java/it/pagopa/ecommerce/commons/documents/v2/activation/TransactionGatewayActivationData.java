package it.pagopa.ecommerce.commons.documents.v2.activation;

/**
 * Extensible interface to handle multiple payment gateway transaction activated
 * data
 */
public sealed interface TransactionGatewayActivationData permits EmptyTransactionGatewayActivationData,NpgTransactionGatewayActivationData {
}
