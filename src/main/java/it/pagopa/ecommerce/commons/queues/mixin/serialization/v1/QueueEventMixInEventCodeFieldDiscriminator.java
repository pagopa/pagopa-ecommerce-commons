package it.pagopa.ecommerce.commons.queues.mixin.serialization.v1;

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

public abstract class QueueEventMixInEventCodeFieldDiscriminator<T extends BaseTransactionEvent<?>> {

    /**
     * No-args constructor
     */
    protected QueueEventMixInEventCodeFieldDiscriminator() {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "eventCode")
    @JsonTypeIdResolver(TransactionEventTypeResolver.class)
    private T event;

}
