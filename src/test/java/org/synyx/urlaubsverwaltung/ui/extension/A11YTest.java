package org.synyx.urlaubsverwaltung.ui.extension;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Base configuration for UI tests.
 *
 * <p>Default configuration:</p>
 * <ul>
 *     <li>Runs tests in {@code chromium} browser</li>
 *     <li>Starts browser with {@code de} locale</li>
 *     <li>Saves record videos of failed tests in {@code "target"} directory</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * @UiTest
 * public class TestExampleUIIT {
 *   @Test
 *   void shouldProvidePage(Page page) {
 *     page.navigate("https://playwright.dev");
 *     assertThat(page).hasURL("https://playwright.dev/");
 *   }
 *
 *   @Test
 *   void shouldResolvePlaywrightObjects(Page page, BrowserContext context, Browser browser) {
 *     assertEquals(context, page.context());
 *     assertEquals(browser, context.browser());
 *     assertNotNull(browser.version());
 *   }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Tag("a11y")
public @interface A11YTest {
}
