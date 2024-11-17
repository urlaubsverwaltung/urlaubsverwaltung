package org.synyx.urlaubsverwaltung.ui.extension;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Paths;

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
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@UsePlaywright(UiTest.CustomOptions.class)
@ExtendWith({ PlaywrightTraceExtension.class, TestRecordVideoExtension.class })
@ContextConfiguration(initializers = UITestInitializer.class)
@Tag("ui")
public @interface UiTest {

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
                    .setRecordVideoDir(Paths.get("target/ui-test", browser))
                    .setLocale("de")
                    .setScreenSize(1500, 2000)
                    .setViewportSize(1500, 2000)
                );
        }
    }
}
