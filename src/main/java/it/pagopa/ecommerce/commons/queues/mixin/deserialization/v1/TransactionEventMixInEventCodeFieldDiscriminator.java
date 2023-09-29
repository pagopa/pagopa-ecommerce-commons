package it.pagopa.ecommerce.commons.queues.mixin.deserialization.v1;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import it.pagopa.ecommerce.commons.documents.v1.serialization.TransactionEventTypeResolver;

/**
 * Mixin class used to properly deserialize
 * {@link it.pagopa.ecommerce.commons.queues.QueueEvent} with `_class` property
 * discriminator
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "eventCode", visible = false)
@JsonTypeIdResolver(TransactionEventTypeResolver.class)
public abstract class TransactionEventMixInEventCodeFieldDiscriminator {

}
