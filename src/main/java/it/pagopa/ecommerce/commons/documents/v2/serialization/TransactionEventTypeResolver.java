package it.pagopa.ecommerce.commons.documents.v2.serialization;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import it.pagopa.ecommerce.commons.documents.v2.TransactionEvent;
import it.pagopa.ecommerce.commons.domain.TransactionId;
import it.pagopa.ecommerce.commons.domain.v2.TransactionEventCode;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Jackson type resolver for deserializing {@link TransactionEvent}s
 */
public class TransactionEventTypeResolver extends TypeIdResolverBase {
    private static final Map<Class<? extends TransactionEvent<?>>, TransactionEventCode> CLASS_TO_EVENT_CODE_MAP;

    private static final Map<TransactionEventCode, Class<? extends TransactionEvent<?>>> EVENT_CODE_TO_CLASS_MAP;

    private static final String BASE_PACKAGE = "it/pagopa/ecommerce/commons/documents/v2";

    private JavaType superType;

    static {
        Tuple2<Map<Class<? extends TransactionEvent<?>>, TransactionEventCode>, Map<TransactionEventCode, Class<? extends TransactionEvent<?>>>> classToEventCodeMappings = initializeEventCodeToClassAssociations(
                BASE_PACKAGE
        );

        CLASS_TO_EVENT_CODE_MAP = classToEventCodeMappings._1();
        EVENT_CODE_TO_CLASS_MAP = classToEventCodeMappings._2();

        checkEventCodeToClassAssociations(BASE_PACKAGE, CLASS_TO_EVENT_CODE_MAP, EVENT_CODE_TO_CLASS_MAP);
    }

    private static Tuple2<Map<Class<? extends TransactionEvent<?>>, TransactionEventCode>, Map<TransactionEventCode, Class<? extends TransactionEvent<?>>>> initializeEventCodeToClassAssociations(
                                                                                                                                                                                                   String basePackage
    ) {
        final Map<Class<? extends TransactionEvent<?>>, TransactionEventCode> classToEventCodeMap = generateClassToEventMap(
                basePackage
        );

        final Map<TransactionEventCode, Class<? extends TransactionEvent<?>>> eventCodeToClassMap = classToEventCodeMap
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
                                                            classToEventCodeMap.get(class1),
                                                            class1.getName(),
                                                            class2.getName()
                                                    )
                                    );
                                }
                        )
                );

        return Tuple.of(classToEventCodeMap, eventCodeToClassMap);
    }

    private static void checkEventCodeToClassAssociations(
                                                          String basePackage,
                                                          Map<Class<? extends TransactionEvent<?>>, TransactionEventCode> classToEventCodeMap,
                                                          Map<TransactionEventCode, Class<? extends TransactionEvent<?>>> eventCodeToClassMap
    ) {
        /* Check that all event codes are present inside the maps */
        Set<TransactionEventCode> eventCodes = Arrays.stream(TransactionEventCode.values()).collect(Collectors.toSet());

        assert classToEventCodeMap.values().containsAll(eventCodes)
                : "Invalid association `v2.TransactionEventCode` <-> `v2.TransactionEvent`! Missing event codes: "
                        + eventCodes.stream()
                                .filter(c -> !classToEventCodeMap.containsValue(c)).collect(Collectors.toSet());

        assert eventCodeToClassMap.keySet().containsAll(eventCodes)
                : "Invalid association `v2.TransactionEventCode` <-> `v2.TransactionEvent`! Missing event codes: "
                        + eventCodes.stream()
                                .filter(c -> !eventCodeToClassMap.containsKey(c)).collect(Collectors.toSet());

        /* Check that all classes are present inside the maps */
        Set<Class<? extends TransactionEvent<?>>> eventClasses = getEventClasses(basePackage);

        assert classToEventCodeMap.keySet().containsAll(eventClasses)
                : "Invalid association `v2.TransactionEventCode` <-> `v2.TransactionEvent`! Missing classes: "
                        + eventClasses.stream().filter(c -> !classToEventCodeMap.containsKey(c))
                                .collect(Collectors.toSet());

        assert eventCodeToClassMap.values().containsAll(eventClasses)
                : "Invalid association `v2.TransactionEventCode` <-> `v2.TransactionEvent`! Missing classes: "
                        + eventClasses.stream()
                                .filter(c -> !eventCodeToClassMap.containsValue(c)).collect(Collectors.toSet());

        /*
         * Given that maps cannot have duplicate keys and these maps are constructed to
         * be each the inverse of the other the above checks are sufficient to guarantee
         * a 1:1 correspondence between `v2.TransactionEvent`s and
         * `v2.TransactionEventCode`s
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
                    "Cannot use `TransactionEventTypeResolver` for classes that do not extend v2.TransactionEvent!"
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

    private static Set<Class<? extends TransactionEvent<?>>> getEventClasses(String basePackage) {
        /*
         * Check that all classes that inherit from `TransactionEvent` are present
         * https://stackoverflow.com/a/495851
         */
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(TransactionEvent.class));

        return provider
                .findCandidateComponents(basePackage)
                .stream()
                .map(c -> {
                    try {
                        return ((Class<? extends TransactionEvent<?>>) Class.forName(c.getBeanClassName()));
                    } catch (ClassNotFoundException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                .collect(Collectors.toSet());
    }

    private static Map<Class<? extends TransactionEvent<?>>, TransactionEventCode> generateClassToEventMap(
                                                                                                           String basePackage
    ) {
        Set<Class<? extends TransactionEvent<?>>> transactionEventClasses = getEventClasses(basePackage);

        return transactionEventClasses.stream()
                .map(transactionEventClass -> {
                    try {
                        Constructor<? extends TransactionEvent<?>> constructor = (Constructor<? extends TransactionEvent<?>>) Arrays
                                .stream(transactionEventClass.getConstructors())
                                .toList()
                                .stream()
                                .filter(c -> c.getParameterTypes().length != 0)
                                .findFirst()
                                .orElseThrow();

                        List<Object> constructorArguments = new ArrayList<>(
                                List.of(new TransactionId(UUID.randomUUID()).value())
                        );
                        int numberOfOtherParams = constructor.getParameterCount() - 1;

                        for (int i = 0; i < numberOfOtherParams; i++) {
                            constructorArguments.add(null);
                        }
                        TransactionEventCode transactionEventCode = constructor
                                .newInstance(constructorArguments.toArray()).getEventCode();

                        return Tuple.of(transactionEventClass, transactionEventCode);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new IllegalStateException(e);
                    }
                }).collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));
    }

}
