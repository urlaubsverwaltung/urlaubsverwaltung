package org.synyx.urlaubsverwaltung;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import de.focus_shift.launchpad.api.HasLaunchpad;
import org.springframework.stereotype.Controller;
import org.synyx.urlaubsverwaltung.search.HasPersonSearch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "org.synyx.urlaubsverwaltung")
class GlobalPersonSearchArchTest {

    /**
     * The global person search is an opt-in: only a {@link HasPersonSearch} controller gets the search box rendered
     * into the page header. That opt-in is easy to forget when a new page is added, and a missing one is invisible
     * until someone looks for the search box (see gh-6622).
     *
     * <p>
     * A controller that renders a full page already opts into the launchpad, which sits in the very same header, so
     * {@link HasLaunchpad} is the marker of "this controller renders a page" and both opt-ins belong together.
     */
    @ArchTest
    void ensureControllersRenderingAPageOptIntoTheGlobalPersonSearch(JavaClasses classes) {

        classes()
            .that().areAnnotatedWith(Controller.class)
            .and().implement(HasLaunchpad.class)
            // csv download, no page is rendered
            .and().doNotHaveFullyQualifiedName("org.synyx.urlaubsverwaltung.application.export.ApplicationForLeaveExportViewController")
            // only redirects and turbo frame fragments of a page rendered elsewhere
            .and().doNotHaveFullyQualifiedName("org.synyx.urlaubsverwaltung.person.web.PersonDeleteViewController")
            // only redirects to the first settings page
            .and().doNotHaveFullyQualifiedName("org.synyx.urlaubsverwaltung.settings.SettingsViewController")
            .should().implement(HasPersonSearch.class)
            .as("Controllers rendering a page must implement HasPersonSearch, otherwise their page has no global person search.")
            .check(classes);
    }
}
