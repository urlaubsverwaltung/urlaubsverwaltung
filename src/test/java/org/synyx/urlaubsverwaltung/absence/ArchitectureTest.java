package org.synyx.urlaubsverwaltung.absence;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.ImportOptions;
import com.tngtech.archunit.library.dependencies.SliceAssignment;
import com.tngtech.archunit.library.dependencies.SliceIdentifier;
import com.tngtech.archunit.library.dependencies.SliceRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

import org.junit.Ignore;
import org.junit.Test;


public class ArchitectureTest {

    private ImportOptions options = new ImportOptions().with(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
            .with(ImportOption.Predefined.DO_NOT_INCLUDE_ARCHIVES).with(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS);

    private JavaClasses classes = new ClassFileImporter(options).importPackages("org.synyx.urlaubsverwaltung");

    @Test
    @Ignore("does not run successfully, yet")
    public void assertNoCyclicPackageDependenciesAtAll() {

        SliceRule rule = SlicesRuleDefinition.slices()
                .assignedFrom(inSliceOneOrTwo())
                .namingSlices("$1")
                .should().beFreeOfCycles().because("we want to be aware of dependency cycles between slices.");

        rule.check(classes);
    }


    private static SliceAssignment inSliceOneOrTwo() {

        return new SliceAssignment() {

            @Override
            public String getDescription() {

                return "absence or sicknote";
            }


            @Override
            public SliceIdentifier getIdentifierOf(JavaClass javaClass) {

                String absencePackageName = "absence";

                if (javaClass.getPackageName().contains(absencePackageName)) {
                    return SliceIdentifier.of(absencePackageName);
                }

                String sicknotePackageName = "sicknote";

                if (javaClass.getPackageName().contains(sicknotePackageName)) {
                    return SliceIdentifier.of(sicknotePackageName);
                }

                return SliceIdentifier.ignore();
            }
        };
    }
}
