package org.synyx.urlaubsverwaltung.ui.extension;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Tracing;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.nio.file.Paths;

import static com.microsoft.playwright.impl.junit.BrowserContextExtension.getOrCreateBrowserContext;

class PlaywrightTraceExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {

        final BrowserContext context = getOrCreateBrowserContext(extensionContext);

        context.tracing().start(new Tracing.StartOptions()
            .setScreenshots(true)
            .setSnapshots(true)
            .setSources(true));
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {

        final boolean succeeded = extensionContext.getExecutionException().isEmpty();
        final BrowserContext browserContext = getOrCreateBrowserContext(extensionContext);

        if (succeeded) {
            browserContext.tracing().stop();
        } else {
            final String filename = normalize("target/FAILED-%s.zip".formatted(extensionContext.getDisplayName()));
            browserContext.tracing().stop(new Tracing.StopOptions()
                .setPath(Paths.get(filename)));
        }
    }

    private static String normalize(String original) {
        return original.replaceAll(" ", "_");
    }
}
