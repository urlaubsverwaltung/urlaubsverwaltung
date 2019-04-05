package org.synyx.urlaubsverwaltung.absence;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchRule;

import org.junit.runner.RunWith;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;


@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = { "org.synyx.urlaubsverwaltung" })
public class ArchitectureTest {

    /*
    @ArchTest
    public static final ArchRule no_sicknote_in_absence = noClasses().that()
            .resideInAPackage("..absence..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..sicknote..");

     */
    @ArchTest
    public static final ArchRule no_sicknote_in_absence = slices().matching("org.synyx.urlaubsverwaltung.(*)..")
            .should()
            .beFreeOfCycles();
}
