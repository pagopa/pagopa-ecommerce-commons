package it.pagopa.ecommerce.commons.queues.mixin.deserialization;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import it.pagopa.ecommerce.commons.documents.v2.serialization.TransactionEventTypeResolver;

/**
 * Mixin class used to properly deserialize
 * {@link it.pagopa.ecommerce.commons.queues.QueueEvent} with `_class` property
 * discriminator
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "_class", visible = false)
@JsonTypeIdResolver(TransactionEventTypeResolver.class)
public abstract class TransactionEventMixIn {

}
