package org.synyx.urlaubsverwaltung;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.ConditionalOnSingleTenantMode;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(
    packages = "org.synyx.urlaubsverwaltung",
    importOptions = {DoNotIncludeTests.class}
)
class SingleTenantArchTest {


    @ArchTest
    void ensureSchedulingConfigurerIsAnnotatedWithConditional(JavaClasses classes) {
        classes()
            .that().implement(SchedulingConfigurer.class)
            .should().beAnnotatedWith(ConditionalOnSingleTenantMode.class)
            .check(classes);
    }


    @ArchTest
    void ensureApplicationStartedOrReadyEventListenerClassesAreAnnotatedWithConditional(JavaClasses classes) {

        final DescribedPredicate<JavaMethod> listeningToApplicationStarted =
            describe("is annotated with @EventListener(ApplicationStartedEvent)", isListeningTo(ApplicationStartedEvent.class));

        final DescribedPredicate<JavaMethod> listeningToApplicationReady =
            describe("is annotated with @EventListener(ApplicationReadyEvent)", isListeningTo(ApplicationReadyEvent.class));

        classes()
            .that().containAnyMethodsThat(listeningToApplicationReady)
            .or().containAnyMethodsThat(listeningToApplicationStarted)
            .should().beAnnotatedWith(ConditionalOnSingleTenantMode.class)
            .check(classes);
    }

    private static Predicate<JavaMethod> isListeningTo(Class<?> event) {
        return method ->
            method.getAnnotations().stream()
                .anyMatch(annotation -> as(annotation, EventListener.class)
                    .map(eventListener -> contains(method.getRawParameterTypes(), event) || contains(eventListener.classes(), event) || contains(eventListener.value(), event))
                    .orElse(false));
    }

    private static boolean contains(Class<?>[] classes, Class<?> event) {
        return List.of(classes).contains(event);
    }

    private static boolean contains(List<JavaClass> javaClasses, Class<?> event) {
        return javaClasses.stream().anyMatch(c -> c.isAssignableTo(event));
    }

    private static <A extends Annotation> Optional<A> as(JavaAnnotation<?> annotation, Class<A> annotationType) {
        if (isEquivalentTo(annotation, annotationType)) {
            return Optional.of(annotation.as(annotationType));
        } else {
            return Optional.empty();
        }
    }

    private static boolean isEquivalentTo(JavaAnnotation<?> annotation, Class<?> annotationType) {
        return annotation.getRawType().isEquivalentTo(annotationType);
    }
}
