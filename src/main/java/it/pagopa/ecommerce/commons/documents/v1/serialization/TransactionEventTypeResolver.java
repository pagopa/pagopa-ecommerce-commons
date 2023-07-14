package it.pagopa.ecommerce.commons.documents.v1.serialization;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import it.pagopa.ecommerce.commons.documents.v1.*;
import it.pagopa.ecommerce.commons.domain.v1.TransactionEventCode;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Jackson type resolver for deserializing {@link TransactionEvent}s
 */
public class TransactionEventTypeResolver extends TypeIdResolverBase {
    private JavaType superType;

    private static final Map<Class<? extends TransactionEvent<?>>, TransactionEventCode> CLASS_TO_EVENT_CODE_MAP = Map
            .ofEntries(
                    Map.entry(TransactionActivatedEvent.class, TransactionEventCode.TRANSACTION_ACTIVATED_EVENT),
                    Map.entry(
                            TransactionAuthorizationCompletedEvent.class,
                            TransactionEventCode.TRANSACTION_AUTHORIZATION_COMPLETED_EVENT
                    ),
                    Map.entry(
                            TransactionAuthorizationRequestedEvent.class,
                            TransactionEventCode.TRANSACTION_AUTHORIZATION_REQUESTED_EVENT
                    ),
                    Map.entry(TransactionClosedEvent.class, TransactionEventCode.TRANSACTION_CLOSED_EVENT),
                    Map.entry(
                            TransactionClosureFailedEvent.class,
                            TransactionEventCode.TRANSACTION_CLOSURE_FAILED_EVENT
                    ),
                    Map.entry(TransactionClosureErrorEvent.class, TransactionEventCode.TRANSACTION_CLOSURE_ERROR_EVENT),
                    Map.entry(
                            TransactionClosureRetriedEvent.class,
                            TransactionEventCode.TRANSACTION_CLOSURE_RETRIED_EVENT
                    ),
                    Map.entry(TransactionExpiredEvent.class, TransactionEventCode.TRANSACTION_EXPIRED_EVENT),
                    Map.entry(TransactionRefundErrorEvent.class, TransactionEventCode.TRANSACTION_REFUND_ERROR_EVENT),
                    Map.entry(
                            TransactionRefundRequestedEvent.class,
                            TransactionEventCode.TRANSACTION_REFUND_REQUESTED_EVENT
                    ),
                    Map.entry(
                            TransactionRefundRetriedEvent.class,
                            TransactionEventCode.TRANSACTION_REFUND_RETRIED_EVENT
                    ),
                    Map.entry(TransactionRefundedEvent.class, TransactionEventCode.TRANSACTION_REFUNDED_EVENT),
                    Map.entry(
                            TransactionUserReceiptRequestedEvent.class,
                            TransactionEventCode.TRANSACTION_USER_RECEIPT_REQUESTED_EVENT
                    ),
                    Map.entry(TransactionUserCanceledEvent.class, TransactionEventCode.TRANSACTION_USER_CANCELED_EVENT),
                    Map.entry(
                            TransactionUserReceiptAddErrorEvent.class,
                            TransactionEventCode.TRANSACTION_ADD_USER_RECEIPT_ERROR_EVENT
                    ),
                    Map.entry(
                            TransactionUserReceiptAddRetriedEvent.class,
                            TransactionEventCode.TRANSACTION_ADD_USER_RECEIPT_RETRY_EVENT
                    ),
                    Map.entry(
                            TransactionUserReceiptAddedEvent.class,
                            TransactionEventCode.TRANSACTION_USER_RECEIPT_ADDED_EVENT
                    )
            );

    private static final Map<TransactionEventCode, Class<? extends TransactionEvent<?>>> EVENT_CODE_TO_CLASS_MAP = CLASS_TO_EVENT_CODE_MAP
            .entrySet().stream().collect(
                    Collectors.toMap(
                            Map.Entry::getValue,
                            Map.Entry::getKey,
                            (
                             class1,
                             class2
                            ) -> {
                                throw new IllegalStateException(
                                        "Cannot have more than one class associated to a single event code! Found ambiguous association: %s => [%s, %s]"
                                                .formatted(
                                                        CLASS_TO_EVENT_CODE_MAP.get(class1),
                                                        class1.getName(),
                                                        class2.getName()
                                                )
                                );
                            }
                    )
            );

    static {
        /* Check that all event codes are present inside the maps */
        Set<TransactionEventCode> eventCodes = Arrays.stream(TransactionEventCode.values()).collect(Collectors.toSet());

        Set<TransactionEventCode> missingEventCodesTargets = eventCodes.stream()
                .filter(c -> !CLASS_TO_EVENT_CODE_MAP.containsValue(c)).collect(Collectors.toSet());
        assert CLASS_TO_EVENT_CODE_MAP.values().containsAll(eventCodes)
                : "Invalid association `v1.TransactionEventCode` <-> `v1.TransactionEvent`! Missing event codes: "
                        + missingEventCodesTargets;

        Set<TransactionEventCode> missingEventCodesSource = eventCodes.stream()
                .filter(c -> !EVENT_CODE_TO_CLASS_MAP.containsKey(c)).collect(Collectors.toSet());
        assert EVENT_CODE_TO_CLASS_MAP.keySet().containsAll(eventCodes)
                : "Invalid association `v1.TransactionEventCode` <-> `v1.TransactionEvent`! Missing event codes: "
                        + missingEventCodesSource;

        /* Check that all classes are present inside the maps */
        Set<Class<? extends TransactionEvent<?>>> eventClasses = getEventClasses();

        Set<Class<?>> missingClassesSource = eventClasses.stream().filter(c -> !CLASS_TO_EVENT_CODE_MAP.containsKey(c))
                .collect(Collectors.toSet());
        assert CLASS_TO_EVENT_CODE_MAP.keySet().containsAll(eventClasses)
                : "Invalid association `v1.TransactionEventCode` <-> `v1.TransactionEvent`! Missing classes: "
                        + missingClassesSource;

        Set<Class<?>> missingClassesTargets = eventClasses.stream()
                .filter(c -> !EVENT_CODE_TO_CLASS_MAP.containsValue(c)).collect(Collectors.toSet());
        assert EVENT_CODE_TO_CLASS_MAP.values().containsAll(eventClasses)
                : "Invalid association `v1.TransactionEventCode` <-> `v1.TransactionEvent`! Missing classes: "
                        + missingClassesTargets;

        /*
         * Given that maps cannot have duplicate keys and these maps are constructed to
         * be each the inverse of the other the above checks are sufficient to guarantee
         * a 1:1 correspondence between `v1.TransactionEvent`s and
         * `v1.TransactionEventCode`s
         */
    }

    @Override
    public void init(JavaType baseType) {
        this.superType = baseType;
    }

    @Override
    public String idFromValue(Object o) {
        if (o instanceof TransactionEvent<?> event) {
            TransactionEventCode transactionEventCode = CLASS_TO_EVENT_CODE_MAP.get(event.getClass());

            if (transactionEventCode == null) {
                throw new IllegalArgumentException("Missing event code for class of type " + o.getClass());
            }

            return transactionEventCode.toString();
        } else {
            throw new IllegalArgumentException(
                    "Cannot use `TransactionEventTypeResolver` for classes that do not extend v1.TransactionEvent!"
            );
        }
    }

    @Override
    public String idFromValueAndType(
                                     Object o,
                                     Class<?> aClass
    ) {
        return idFromValue(o);
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }

    @Override
    public JavaType typeFromId(
                               DatabindContext context,
                               String id
    ) {
        TransactionEventCode eventCode = TransactionEventCode.valueOf(id);
        Class<? extends TransactionEvent<?>> subType = EVENT_CODE_TO_CLASS_MAP.get(eventCode);

        if (subType == null) {
            throw new IllegalArgumentException(
                    "Cannot find TransactionEvent class for event code %s!".formatted(eventCode)
            );
        }

        return context.constructSpecializedType(superType, subType);
    }

    private static Set<Class<? extends TransactionEvent<?>>> getEventClasses() {
        /*
         * Check that all classes that inherit from `TransactionEvent` are present
         * https://stackoverflow.com/a/495851
         */
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(TransactionEvent.class));

        Set<Class<? extends TransactionEvent<?>>> eventClasses = provider
                .findCandidateComponents("it/pagopa/ecommerce/commons/documents/v1/")
                .stream()
                .map(c -> {
                    try {
                        return ((Class<? extends TransactionEvent<?>>) Class.forName(c.getBeanClassName()));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(c -> Modifier.isAbstract(c.getModifiers()))
                .collect(Collectors.toSet());
        return eventClasses;
    }
}
