package it.pagopa.ecommerce.commons.queues;

import it.pagopa.ecommerce.commons.documents.BaseTransactionEvent;

/**
 * <p>
 * Wrapper class for events to be serialized in queues.
 * </p>
 * <p>
 * This class reifies W3C headers in the serialized event to work around Azure
 * Storage Queue limitations (as of 2023-06-27).
 * </p>
 * <p>
 * See also: <a href=
 * "https://github.com/Azure/azure-functions-dotnet-worker/issues/1126#issuecomment-1601677068">this
 * issue</a>.
 * </p>
 *
 * @param event       wrapped event
 * @param tracingInfo W3C tracing information (ref.
 *                    <a href="https://w3c.github.io/trace-context/">Trace
 *                    Context Level 3 Spec</a>). Clients reading events are
 *                    required to override incoming tracing information (if any)
 *                    and propagate the ones provided here
 * @param <T>         type of the wrapped event
 */
public record QueueEvent<T extends BaseTransactionEvent<?>> (
        T event,
        TracingInfo tracingInfo
) {
}
