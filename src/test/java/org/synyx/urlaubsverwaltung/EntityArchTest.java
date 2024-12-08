package org.synyx.urlaubsverwaltung;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import jakarta.persistence.Entity;
import org.synyx.urlaubsverwaltung.tenancy.tenant.AbstractTenantAwareEntity;

@AnalyzeClasses(packages = "org.synyx.urlaubsverwaltung")
class EntityArchTest {

    @ArchTest
    void ensureEntityClassesExtendAbstractTenantAwareEntity(JavaClasses classes) {
        ArchRuleDefinition.classes()
            .that().areAnnotatedWith(Entity.class)
            .should().beAssignableTo(AbstractTenantAwareEntity.class)
            .andShould().bePublic()
            .check(classes);
    }

}
