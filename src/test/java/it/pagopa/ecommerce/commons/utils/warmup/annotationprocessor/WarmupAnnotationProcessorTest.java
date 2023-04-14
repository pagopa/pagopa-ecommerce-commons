package it.pagopa.ecommerce.commons.utils.warmup.annotationprocessor;

import it.pagopa.ecommerce.commons.annotations.Warmup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class WarmupAnnotationProcessorTest {

    private final ProcessingEnvironment processingEnv = Mockito.mock(ProcessingEnvironment.class);

    private final RoundEnvironment roundEnv = Mockito.mock(RoundEnvironment.class);

    private final Element element = Mockito.mock(Element.class);

    private final ExecutableElement executableElement = Mockito.mock(ExecutableElement.class);

    private final VariableElement variableElement = Mockito.mock(VariableElement.class);

    private final Element warmupMethodEnclosingElement = Mockito.mock(Element.class);

    private final Name executableElementName = Mockito.mock(Name.class);

    private final Messager messager = Mockito.mock(Messager.class);

    private final WarmupAnnotationProcessor warmupAnnotationProcessor = new WarmupAnnotationProcessor();

    private final RestController restControllerAnnotation = Mockito.mock(RestController.class);

    @Test
    void shouldRaiseErrorForWarmupMethodWithArgParameters() {
        /*
         * pre-requisite
         */
        Mockito.when(processingEnv.getMessager()).thenReturn(messager);
        Mockito.when(roundEnv.getElementsAnnotatedWith(Warmup.class)).thenReturn((Set) Set.of(executableElement));
        Mockito.when(executableElement.getEnclosingElement()).thenReturn(warmupMethodEnclosingElement);
        Mockito.when(executableElement.getSimpleName()).thenReturn(executableElementName);
        Mockito.when(executableElementName.toString()).thenReturn("executableElementName");
        Mockito.when(executableElement.getParameters()).thenReturn((List) List.of(variableElement));
        Mockito.when(executableElement.getModifiers()).thenReturn(Set.of(Modifier.PUBLIC));
        Mockito.when(warmupMethodEnclosingElement.getAnnotation(RestController.class))
                .thenReturn(restControllerAnnotation);
        /*
         * Test
         */
        warmupAnnotationProcessor.init(processingEnv);
        warmupAnnotationProcessor.process(Collections.emptySet(), roundEnv);

        /*
         * Assertions
         */
        Mockito.verify(messager, Mockito.times(1)).printMessage(
                eq(Diagnostic.Kind.ERROR),
                argThat(
                        message -> message.equals(
                                "Warmup method: [warmupMethodEnclosingElement.executableElementName] should not have arguments"
                        )
                )
        );
        Mockito.verify(messager, Mockito.times(1)).printMessage(
                eq(Diagnostic.Kind.ERROR),
                any()
        );
    }

    @Test
    void shouldRaiseErrorForWarmupMethodInClassThatHasNotRestControllerAnnotation() {
        /*
         * pre-requisite
         */
        Mockito.when(processingEnv.getMessager()).thenReturn(messager);
        Mockito.when(roundEnv.getElementsAnnotatedWith(Warmup.class)).thenReturn((Set) Set.of(executableElement));
        Mockito.when(executableElement.getEnclosingElement()).thenReturn(warmupMethodEnclosingElement);
        Mockito.when(executableElement.getSimpleName()).thenReturn(executableElementName);
        Mockito.when(executableElementName.toString()).thenReturn("executableElementName");
        Mockito.when(executableElement.getParameters()).thenReturn(List.of());
        Mockito.when(executableElement.getModifiers()).thenReturn(Set.of(Modifier.PUBLIC));
        Mockito.when(warmupMethodEnclosingElement.getAnnotation(RestController.class)).thenReturn(null);
        /*
         * Test
         */
        warmupAnnotationProcessor.init(processingEnv);
        boolean returnValue = warmupAnnotationProcessor.process(Collections.emptySet(), roundEnv);

        /*
         * Assertions
         */
        assertTrue(returnValue);
        Mockito.verify(messager, Mockito.times(1)).printMessage(
                eq(Diagnostic.Kind.ERROR),
                argThat(
                        message -> message.equals(
                                "Found warmup method in class [warmupMethodEnclosingElement] but is not annotated with @RestController"
                        )
                )
        );
        Mockito.verify(messager, Mockito.times(1)).printMessage(
                eq(Diagnostic.Kind.ERROR),
                any()
        );
    }

    @Test
    void shouldRaiseErrorForWarmupMethodWithInvalidVisibility() {
        /*
         * pre-requisite
         */
        Mockito.when(processingEnv.getMessager()).thenReturn(messager);
        Mockito.when(roundEnv.getElementsAnnotatedWith(Warmup.class)).thenReturn((Set) Set.of(executableElement));
        Mockito.when(executableElement.getEnclosingElement()).thenReturn(warmupMethodEnclosingElement);
        Mockito.when(executableElement.getSimpleName()).thenReturn(executableElementName);
        Mockito.when(executableElementName.toString()).thenReturn("executableElementName");
        Mockito.when(executableElement.getParameters()).thenReturn(List.of());
        Mockito.when(executableElement.getModifiers()).thenReturn(Set.of(Modifier.PRIVATE));
        Mockito.when(warmupMethodEnclosingElement.getAnnotation(RestController.class))
                .thenReturn(restControllerAnnotation);
        /*
         * Test
         */
        warmupAnnotationProcessor.init(processingEnv);
        boolean returnValue = warmupAnnotationProcessor.process(Collections.emptySet(), roundEnv);

        /*
         * Assertions
         */
        assertTrue(returnValue);
        Mockito.verify(messager, Mockito.times(1)).printMessage(
                eq(Diagnostic.Kind.ERROR),
                argThat(
                        message -> message.equals(
                                "Warmup method: [warmupMethodEnclosingElement.executableElementName] should have only public modifier"
                        )
                )
        );
        Mockito.verify(messager, Mockito.times(1)).printMessage(
                eq(Diagnostic.Kind.ERROR),
                any()
        );
    }

    @Test
    void shouldRaiseErrorForWarmupMethodWithInvalidModifiers() {
        /*
         * pre-requisite
         */
        Mockito.when(processingEnv.getMessager()).thenReturn(messager);
        Mockito.when(roundEnv.getElementsAnnotatedWith(Warmup.class)).thenReturn((Set) Set.of(executableElement));
        Mockito.when(executableElement.getEnclosingElement()).thenReturn(warmupMethodEnclosingElement);
        Mockito.when(executableElement.getSimpleName()).thenReturn(executableElementName);
        Mockito.when(executableElementName.toString()).thenReturn("executableElementName");
        Mockito.when(executableElement.getParameters()).thenReturn(List.of());
        Mockito.when(executableElement.getModifiers()).thenReturn(Set.of(Modifier.PRIVATE, Modifier.STATIC));
        Mockito.when(warmupMethodEnclosingElement.getAnnotation(RestController.class))
                .thenReturn(restControllerAnnotation);
        /*
         * Test
         */
        warmupAnnotationProcessor.init(processingEnv);
        boolean returnValue = warmupAnnotationProcessor.process(Collections.emptySet(), roundEnv);

        /*
         * Assertions
         */
        assertTrue(returnValue);
        Mockito.verify(messager, Mockito.times(1)).printMessage(
                eq(Diagnostic.Kind.ERROR),
                argThat(
                        message -> message.equals(
                                "Warmup method: [warmupMethodEnclosingElement.executableElementName] should have only public modifier"
                        )
                )
        );
        Mockito.verify(messager, Mockito.times(1)).printMessage(
                eq(Diagnostic.Kind.ERROR),
                any()
        );
    }

    @Test
    void shouldRaiseErrorForAnnotationAppliedOnInvalidElement() {
        /*
         * pre-requisite
         */
        Mockito.when(processingEnv.getMessager()).thenReturn(messager);
        Mockito.when(roundEnv.getElementsAnnotatedWith(Warmup.class)).thenReturn((Set) Set.of(variableElement));
        Mockito.when(variableElement.getKind()).thenReturn(ElementKind.FIELD);
        Mockito.when(warmupMethodEnclosingElement.getAnnotation(RestController.class))
                .thenReturn(restControllerAnnotation);
        /*
         * Test
         */
        warmupAnnotationProcessor.init(processingEnv);
        boolean returnValue = warmupAnnotationProcessor.process(Collections.emptySet(), roundEnv);

        /*
         * Assertions
         */
        assertTrue(returnValue);
        Mockito.verify(messager, Mockito.times(1)).printMessage(
                eq(Diagnostic.Kind.ERROR),
                argThat(
                        message -> message.equals(
                                "Invalid annotation location, annotation expected on method but found on: [FIELD]"
                        )
                )
        );
        Mockito.verify(messager, Mockito.times(1)).printMessage(
                eq(Diagnostic.Kind.ERROR),
                any()
        );
    }

    @Test
    void shouldRaiseAllValidationErrors() {
        /*
         * pre-requisite
         */
        Mockito.when(processingEnv.getMessager()).thenReturn(messager);
        Mockito.when(roundEnv.getElementsAnnotatedWith(Warmup.class)).thenReturn((Set) Set.of(executableElement));
        Mockito.when(executableElement.getEnclosingElement()).thenReturn(warmupMethodEnclosingElement);
        Mockito.when(executableElement.getSimpleName()).thenReturn(executableElementName);
        Mockito.when(executableElementName.toString()).thenReturn("executableElementName");
        Mockito.when(executableElement.getParameters()).thenReturn((List) List.of(variableElement));
        Mockito.when(executableElement.getModifiers()).thenReturn(Set.of(Modifier.PRIVATE));
        Mockito.when(warmupMethodEnclosingElement.getAnnotation(RestController.class)).thenReturn(null);
        /*
         * Test
         */
        warmupAnnotationProcessor.init(processingEnv);
        boolean returnValue = warmupAnnotationProcessor.process(Collections.emptySet(), roundEnv);

        /*
         * Assertions
         */
        assertTrue(returnValue);
        Mockito.verify(messager, Mockito.times(1)).printMessage(
                eq(Diagnostic.Kind.ERROR),
                argThat(
                        message -> message.equals(
                                "Found warmup method in class [warmupMethodEnclosingElement] but is not annotated with @RestController"
                        )
                )
        );
        Mockito.verify(messager, Mockito.times(1)).printMessage(
                eq(Diagnostic.Kind.ERROR),
                argThat(
                        message -> message.equals(
                                "Warmup method: [warmupMethodEnclosingElement.executableElementName] should not have arguments"
                        )
                )
        );
        Mockito.verify(messager, Mockito.times(1)).printMessage(
                eq(Diagnostic.Kind.ERROR),
                argThat(
                        message -> message.equals(
                                "Warmup method: [warmupMethodEnclosingElement.executableElementName] should have only public modifier"
                        )
                )
        );
        Mockito.verify(messager, Mockito.times(3)).printMessage(
                eq(Diagnostic.Kind.ERROR),
                any()
        );
    }

    @Test
    void shouldNotRaiseErrorForMethodAnnotatedWithWarmupInRestControllerAnnotatedClassAndCorrectVisibility() {
        /*
         * pre-requisite
         */
        Mockito.when(processingEnv.getMessager()).thenReturn(messager);
        Mockito.when(roundEnv.getElementsAnnotatedWith(Warmup.class)).thenReturn((Set) Set.of(executableElement));
        Mockito.when(executableElement.getEnclosingElement()).thenReturn(warmupMethodEnclosingElement);
        Mockito.when(executableElement.getSimpleName()).thenReturn(executableElementName);
        Mockito.when(executableElementName.toString()).thenReturn("executableElementName");
        Mockito.when(executableElement.getParameters()).thenReturn(List.of());
        Mockito.when(executableElement.getModifiers()).thenReturn(Set.of(Modifier.PUBLIC));
        Mockito.when(warmupMethodEnclosingElement.getAnnotation(RestController.class))
                .thenReturn(restControllerAnnotation);
        /*
         * Test
         */
        warmupAnnotationProcessor.init(processingEnv);
        boolean returnValue = warmupAnnotationProcessor.process(Collections.emptySet(), roundEnv);

        /*
         * Assertions
         */
        assertTrue(returnValue);
        Mockito.verify(messager, Mockito.times(0)).printMessage(
                eq(Diagnostic.Kind.ERROR),
                argThat(
                        message -> message.equals(
                                "Found warmup method in class [warmupMethodEnclosingElement] but is not annotated with @RestController"
                        )
                )
        );
        Mockito.verify(messager, Mockito.times(0)).printMessage(
                eq(Diagnostic.Kind.ERROR),
                argThat(
                        message -> message.equals(
                                "Warmup method: [warmupMethodEnclosingElement.executableElementName] should not have arguments"
                        )
                )
        );
        Mockito.verify(messager, Mockito.times(0)).printMessage(
                eq(Diagnostic.Kind.ERROR),
                any()
        );
    }
}
