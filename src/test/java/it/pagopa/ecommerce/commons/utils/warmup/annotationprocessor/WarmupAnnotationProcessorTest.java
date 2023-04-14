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
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

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
    }

    @Test
    void shouldRaiseErrorForWarmupMethodWithParametersInClassThatHasNotRestControllerAnnotation() {
        /*
         * pre-requisite
         */
        Mockito.when(processingEnv.getMessager()).thenReturn(messager);
        Mockito.when(roundEnv.getElementsAnnotatedWith(Warmup.class)).thenReturn((Set) Set.of(executableElement));
        Mockito.when(executableElement.getEnclosingElement()).thenReturn(warmupMethodEnclosingElement);
        Mockito.when(executableElement.getSimpleName()).thenReturn(executableElementName);
        Mockito.when(executableElementName.toString()).thenReturn("executableElementName");
        Mockito.when(executableElement.getParameters()).thenReturn((List) List.of(variableElement));
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
    }

    @Test
    void shouldNotRaiseErrorForMethodAnnotatedWithWarmupInRestControllerAnnotatedClass() {
        /*
         * pre-requisite
         */
        Mockito.when(processingEnv.getMessager()).thenReturn(messager);
        Mockito.when(roundEnv.getElementsAnnotatedWith(Warmup.class)).thenReturn((Set) Set.of(executableElement));
        Mockito.when(executableElement.getEnclosingElement()).thenReturn(warmupMethodEnclosingElement);
        Mockito.when(executableElement.getSimpleName()).thenReturn(executableElementName);
        Mockito.when(executableElementName.toString()).thenReturn("executableElementName");
        Mockito.when(executableElement.getParameters()).thenReturn(List.of());
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
    }
}
