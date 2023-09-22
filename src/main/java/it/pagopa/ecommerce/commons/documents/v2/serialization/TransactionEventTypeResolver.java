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
    private static final Map<Class<? extends TransactionEvent<?>>, String> CLASS_TO_PATH_MAP;

    private static final Map<String, Class<? extends TransactionEvent<?>>> PATH_TO_CLASS_MAP;

    private static final String BASE_PACKAGE = "it/pagopa/ecommerce/commons/documents/v2";
    private static final String BASE_PACKAGE_HUMAN_READABLE = BASE_PACKAGE.replace('/', '.');

    private JavaType superType;

    static {
        Tuple2<Map<Class<? extends TransactionEvent<?>>, String>, Map<String, Class<? extends TransactionEvent<?>>>> classToPathMappings = initializePathToClassAssociations(
                BASE_PACKAGE
        );

        CLASS_TO_PATH_MAP = classToPathMappings._1();
        PATH_TO_CLASS_MAP = classToPathMappings._2();
    }

    private static Tuple2<Map<Class<? extends TransactionEvent<?>>, String>, Map<String, Class<? extends TransactionEvent<?>>>> initializePathToClassAssociations(
                                                                                                                                                                  String basePackage
    ) {
        final Map<Class<? extends TransactionEvent<?>>, String> classToEventCodeMap = generateClassToPathMap(
                basePackage
        );

        final Map<String, Class<? extends TransactionEvent<?>>> eventCodeToClassMap = classToEventCodeMap
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

    @Override
    public void init(JavaType baseType) {
        this.superType = baseType;
    }

    @Override
    public String idFromValue(Object o) {
        if (o instanceof TransactionEvent<?> event) {
            String classPath = CLASS_TO_PATH_MAP.get(event.getClass());

            if (classPath == null) {
                throw new IllegalArgumentException("Missing event code for class of type " + o.getClass());
            }

            return classPath;
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
        if (!id.startsWith(BASE_PACKAGE_HUMAN_READABLE)) {
            throw new IllegalArgumentException("Expected v2 transaction, got id: %s".formatted(id));
        }

        Class<? extends TransactionEvent<?>> subType = PATH_TO_CLASS_MAP.get(id);

        if (subType == null) {
            throw new IllegalArgumentException(
                    "Cannot find TransactionEvent class for class %s!".formatted(id)
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

    private static Map<Class<? extends TransactionEvent<?>>, String> generateClassToPathMap(
                                                                                            String basePackage
    ) {
        Set<Class<? extends TransactionEvent<?>>> transactionEventClasses = getEventClasses(basePackage);

        return transactionEventClasses.stream()
                .map(transactionEventClass -> Tuple.of(transactionEventClass, transactionEventClass.getCanonicalName()))
                .collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));
    }

}
