package it.pagopa.ecommerce.commons.utils.warmup.annotationprocessor;

import it.pagopa.ecommerce.commons.annotations.Warmup;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Set;

/**
 * {@link Warmup} annotation processor. This annotation processor checks that
 * the {@link Warmup} annotated method has no arguments and that it's declaring
 * class is a {@link RestController} annotated ones
 */
@SupportedAnnotationTypes("it.pagopa.ecommerce.commons.annotations.WarmupMethod")
public class WarmupAnnotationProcessor extends AbstractProcessor {

    /**
     * Process all annotated methods
     *
     * @param annotations the annotation interfaces requested to be processed
     * @param roundEnv    environment for information about the current and prior
     *                    round
     * @return true for letting other annotations processor scanning other elements
     */
    @Override
    public boolean process(
                           Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv
    ) {

        for (Element element : roundEnv.getElementsAnnotatedWith(Warmup.class)) {
            if (!(element instanceof ExecutableElement executableElement)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Annotation should be on method.");
                continue;
            }

            List<? extends VariableElement> parameters = executableElement.getParameters();
            if (!parameters.isEmpty()) {
                processingEnv.getMessager()
                        .printMessage(Diagnostic.Kind.ERROR, "Warmup Method should not have arguments");
            }
            if (!executableElement.getClass().isAnnotationPresent(RestController.class)) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Warmup Method should be add inside a Controller class annotated with @RestController"
                );
            }
        }
        return true; // no further processing of this annotation type
    }

}
