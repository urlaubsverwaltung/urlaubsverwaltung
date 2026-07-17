package org.synyx.urlaubsverwaltung.ui.extension;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;

/**
 * Bootstraps the infrastructure needed to run a browser-driven test against a real, running application:
 * a Spring test context and a Playwright browser instance.
 *
 * <p>This annotation only wires up the infrastructure. It does not, by itself, categorize the test into a
 * suite that can be selected via JUnit/Maven tags. Combine it with a tag annotation such as {@link UiTest}
 * or {@link A11yTest} to mark what kind of browser test it is.</p>
 *
 * <p>Default configuration:</p>
 * <ul>
 *     <li>Runs tests in {@code chromium} browser (override with the {@code browser} system property)</li>
 *     <li>Starts browser with {@code de} locale</li>
 *     <li>Saves record videos of failed tests in {@code "target"} directory</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * @SpringBootTest(webEnvironment = RANDOM_PORT)
 * @UiIntegrationTest
 * @UiTest
 * class TestExampleUIIT {
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
@Inherited
@UsePlaywright(UiIntegrationTest.CustomOptions.class)
@ExtendWith({
    SpringExtension.class,
    PlaywrightTraceExtension.class,
    TestRecordVideoExtension.class
})
@ContextConfiguration(initializers = UITestInitializer.class)
public @interface UiIntegrationTest {

    class CustomOptions implements OptionsFactory {

        @Override
        public Options getOptions() {

            // webkit | firefox | chromium (playwright-default)
            final String browser = System.getProperty("browser", "chromium");

            return new Options()
                .setBrowserName(browser)
                .setConnectOptions(new BrowserType.ConnectOptions()
                    // increase to make test steps slower and be able to follow it with your own eyes.
                    .setSlowMo(200)
                )
                .setContextOptions(new Browser.NewContextOptions()
                    .setRecordVideoDir(Path.of("target/ui-test", browser))
                    .setLocale("de")
                    .setScreenSize(1500, 2000)
                    .setViewportSize(1500, 2000)
                    .setRecordVideoSize(1500, 2000)
                );
        }
    }
}
