package org.synyx.urlaubsverwaltung.ui.extension;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test as belonging to the "a11y" suite of accessibility checks, so it can be selected or excluded
 * via JUnit/Maven's {@code a11y} tag (e.g. {@code -Dgroups=a11y}). Typically used for tests that crawl
 * pages with axe-core (via Playwright) to assert they are free of accessibility violations.
 *
 * <p>This annotation is a plain tag — it does not configure any test infrastructure by itself. Combine it
 * with {@link UiIntegrationTest} to also get a Spring context and a Playwright browser.</p>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * @SpringBootTest(webEnvironment = RANDOM_PORT)
 * @UiIntegrationTest
 * @A11yTest
 * class TestExampleAccessibilityIT {
 *   @Test
 *   void shouldHaveNoAccessibilityViolations(Page page) {
 *     page.navigate("/");
 *     final AxeResults results = new AxeBuilder(page).analyze();
 *     assertThat(results.getViolations()).isEmpty();
 *   }
 * }
 * }</pre>
 *
 * @see UiTest
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Tag("a11y")
public @interface A11yTest {
}
