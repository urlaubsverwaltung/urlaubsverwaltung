package org.synyx.urlaubsverwaltung.absence;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.ImportOptions;
import com.tngtech.archunit.library.dependencies.SliceRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.Test;


public class ArchitectureTest {

    private ImportOptions options = new ImportOptions()
        .with(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
        .with(ImportOption.Predefined.DO_NOT_INCLUDE_ARCHIVES)
        .with(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS);

    private JavaClasses classes = new ClassFileImporter(options)
        .importPackages("org.synyx.urlaubsverwaltung");

    @Test
    public void assertNoCyclicPackageDependenciesAtAll() {

        SliceRule rule = SlicesRuleDefinition.slices()
            .matching("org.synyx.(urlaubsverwaltung).(**)..")
            .namingSlices("$2 of $1")
            .should()
            .beFreeOfCycles()
            .because("we want to be aware of dependency cycles between slices.");

        rule.check(classes);
    }
}
