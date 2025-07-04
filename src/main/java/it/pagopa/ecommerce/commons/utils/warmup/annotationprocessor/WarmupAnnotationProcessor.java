package it.pagopa.ecommerce.commons.utils.warmup.annotationprocessor;

import it.pagopa.ecommerce.commons.annotations.Warmup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Set;

/**
 * {@link Warmup} annotation processor. This annotation processor checks that
 * the {@link Warmup} annotated method has no arguments and that its declaring
 * class is annotated with {@link RestController}
 */
@SupportedAnnotationTypes("it.pagopa.ecommerce.commons.annotations.Warmup")
public class WarmupAnnotationProcessor extends AbstractProcessor {

    private final Logger logger = LoggerFactory.getLogger(WarmupAnnotationProcessor.class);

    /**
     * No-args constructor
     */
    /*
     * @formatter:off
     *
     * Warning java:S1186 - Methods should not be empty
     * Suppressed because this constructor is required by annotation
     * processing framework and should remain empty
     *
     * @formatter:on
     */
    @SuppressWarnings("java:S1186")
    public WarmupAnnotationProcessor() {
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        logger.info("WarmupAnnotationProcessor initialized");
    }

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
        logger.info("WarmupAnnotationProcessor start process");

        for (Element element : roundEnv.getElementsAnnotatedWith(Warmup.class)) {

            if (element instanceof ExecutableElement executableElement) {
                Element declaringClass = executableElement.getEnclosingElement();
                Set<Modifier> modifiers = executableElement.getModifiers();
                String className = declaringClass.toString();
                String warmupMethod = executableElement.getSimpleName().toString();
                List<? extends VariableElement> parameters = executableElement.getParameters();

                if (!parameters.isEmpty()) {
                    processingEnv.getMessager()
                            .printMessage(
                                    Diagnostic.Kind.ERROR,
                                    "Warmup method: [%s.%s] should not have arguments"
                                            .formatted(className, warmupMethod)
                            );
                }
                if (modifiers.size() != 1 || !modifiers.contains(Modifier.PUBLIC)) {
                    processingEnv.getMessager()
                            .printMessage(
                                    Diagnostic.Kind.ERROR,
                                    "Warmup method: [%s.%s] should have only public modifier"
                                            .formatted(className, warmupMethod)
                            );
                }
                if (declaringClass.getAnnotation(RestController.class) == null) {
                    processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            "Found warmup method in class [%s] but is not annotated with @RestController"
                                    .formatted(className)
                    );
                }
            } else {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Invalid annotation location, annotation expected on method but found on: [%s]"
                                .formatted(element.getKind())
                );
            }

        }
        return true; // no further processing of this annotation type
    }

}
