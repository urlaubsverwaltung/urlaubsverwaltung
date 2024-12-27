package org.synyx.urlaubsverwaltung;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.syntax.elements.GivenMethodsConjunction;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static org.synyx.urlaubsverwaltung.web.HotwiredTurboConstants.TURBO_STREAM_MEDIA_TYPE;

@AnalyzeClasses(packages = "org.synyx.urlaubsverwaltung")
class HotwiredTurboGotchasTest {

    private static final DescribedPredicate<List<JavaClass>> errors =
        describe("containing Errors.class", javaClassesContains(Errors.class));

    private static final GivenMethodsConjunction publicControllerMethods =
        methods()
            .that().areDeclaredInClassesThat().areAnnotatedWith(Controller.class)
            .and().arePublic().or().arePackagePrivate()
            .and().haveRawParameterTypes(errors);

    @ArchTest
    void ensureControllerPostMethodsProducingHotwiredStreams(JavaClasses classes) {

        final DescribedPredicate<JavaAnnotation<?>> postMappingProducingHotwiredStream =
            describe("@PostMapping producing hotwired stream", postMappingProducingHotwiredTurboStream());

        publicControllerMethods
            .and(annotatedWith(postMappingProducingHotwiredStream))
            .should().haveRawReturnType(ModelAndView.class)
            .as("Controller methods annotated with @PostMapping and producing hotwired streams " +
                "must return ModelAndView because form posts returning validation errors are only rendered with HttpStatus.UNPROCESSABLE_ENTITY.")
            .check(classes);
    }

    @ArchTest
    void ensureControllerPostMethodsAcceptingTurboFrameHeader(JavaClasses classes) {

        final DescribedPredicate<JavaMethod> consumesTurboFrameHeader =
            describe("accepting @RequestHeader(name=\"Turbo-Frame\")", acceptingTurboFrameHeader());

        publicControllerMethods
            .and().areAnnotatedWith(PostMapping.class)
            .and(consumesTurboFrameHeader)
            .should().haveRawReturnType(ModelAndView.class)
            .as("Controller methods annotated with @PostMapping and accepting @RequestHeader(\"Turbo-Frame\") " +
                "must return ModelAndView because form posts returning validation errors are only rendered with HttpStatus.UNPROCESSABLE_ENTITY.")
            .allowEmptyShould(true)
            .check(classes);
    }

    private static Predicate<JavaAnnotation<?>> postMappingProducingHotwiredTurboStream() {
        return javaAnnotation -> {
            if (javaAnnotation.getRawType().isAssignableFrom(PostMapping.class)) {
                return javaAnnotation.get("produces")
                    .map(produces -> Arrays.stream((String[]) produces).toList().contains(TURBO_STREAM_MEDIA_TYPE))
                    .orElse(false);
            }
            return false;
        };
    }

    private static Predicate<JavaMethod> acceptingTurboFrameHeader() {
        return method ->
            method.getParameters().stream().anyMatch(javaParameter ->
                javaParameter.tryGetAnnotationOfType(RequestHeader.class)
                    .map(a -> a.name().equals("Turbo-Frame"))
                    .orElse(false));
    }

    private static Predicate<List<JavaClass>> javaClassesContains(Class<?> clazz) {
        return javaClasses -> javaClasses.stream().anyMatch(javaClass -> javaClass.isAssignableTo(clazz));
    }
}
