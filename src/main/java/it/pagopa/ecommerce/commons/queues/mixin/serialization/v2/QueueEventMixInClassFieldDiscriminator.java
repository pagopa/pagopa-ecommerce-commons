package it.pagopa.ecommerce.commons.queues.mixin.serialization.v2;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import it.pagopa.ecommerce.commons.documents.BaseTransactionEvent;
import it.pagopa.ecommerce.commons.documents.v2.serialization.TransactionEventTypeResolver;

/**
 * Mixin class used to properly serialize
 * {@link it.pagopa.ecommerce.commons.queues.QueueEvent} with `_class` property
 * discriminator
 *
 * @param <T> -
 *            {@link it.pagopa.ecommerce.commons.documents.v1.TransactionEvent}
 *            type
 */
public abstract class QueueEventMixInClassFieldDiscriminator<T extends BaseTransactionEvent<?>> {

    /**
     * No-args constructor
     */
    protected QueueEventMixInClassFieldDiscriminator(T event) {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "_class", visible = false)
    @JsonTypeIdResolver(TransactionEventTypeResolver.class)
    private T event;

}
