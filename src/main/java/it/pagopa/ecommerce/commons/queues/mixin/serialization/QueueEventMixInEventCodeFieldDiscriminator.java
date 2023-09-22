package it.pagopa.ecommerce.commons.queues.mixin.serialization;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import it.pagopa.ecommerce.commons.documents.BaseTransactionEvent;
import it.pagopa.ecommerce.commons.documents.v1.serialization.TransactionEventTypeResolver;

/**
 * Mixin class used to properly serialize
 * {@link it.pagopa.ecommerce.commons.queues.QueueEvent} with `eventCode`
 * property discriminator
 *
 * @param <T> -
 *            {@link it.pagopa.ecommerce.commons.documents.v1.TransactionEvent}
 *            type
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "eventCode")
@JsonTypeIdResolver(TransactionEventTypeResolver.class)
public interface QueueEventMixInEventCodeFieldDiscriminator<T extends BaseTransactionEvent<?>> {

}
