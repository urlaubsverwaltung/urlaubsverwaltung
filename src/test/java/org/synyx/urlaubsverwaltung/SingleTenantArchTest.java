package org.synyx.urlaubsverwaltung;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.ConditionalOnSingleTenantMode;

@AnalyzeClasses(
    packages = "org.synyx.urlaubsverwaltung",
    importOptions = { DoNotIncludeTests.class }
)
class SingleTenantArchTest {

    @ArchTest
    void ensureSchedulingConfigurerIsAnnotatedWithConditional(JavaClasses classes) {
        ArchRuleDefinition.classes()
            .that().implement(SchedulingConfigurer.class)
            .should().beAnnotatedWith(ConditionalOnSingleTenantMode.class)
            .check(classes);
    }
}
