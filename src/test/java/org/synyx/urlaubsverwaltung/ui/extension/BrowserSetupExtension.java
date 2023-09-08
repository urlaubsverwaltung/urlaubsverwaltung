package org.synyx.urlaubsverwaltung.ui.extension;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Paths;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.ui.extension.UiTestStore.getBrowser;
import static org.synyx.urlaubsverwaltung.ui.extension.UiTestStore.getBrowserContext;
import static org.synyx.urlaubsverwaltung.ui.extension.UiTestStore.getPage;
import static org.synyx.urlaubsverwaltung.ui.extension.UiTestStore.getPlaywright;

/**
 * Handle stuff like closing the Browser or saving recorded test videos.
 */
public class BrowserSetupExtension implements AfterEachCallback {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    @Override
    public void afterEach(ExtensionContext context) throws Exception {

        final Page page = getPage(context);
        final BrowserContext browserContext = getBrowserContext(context);
        final Browser browser = getBrowser(context);
        final Playwright playwright = getPlaywright(context);

        final File videoFile = new File(page.video().path().toUri());
        final String newVideoFilePath = normalizeVideoFileName("target/%s.webm".formatted(context.getDisplayName()));
        final File newVideoFile = new File(Paths.get(newVideoFilePath).toUri());
        final boolean isMoved = videoFile.renameTo(newVideoFile);
        if (!isMoved) {
            LOG.info("could not rename test video file.");
        }

        page.close();
        browserContext.close();
        browser.close();
        playwright.close();
    }

    private static String normalizeVideoFileName(String original) {
        return original.replaceAll(" ", "_");
    }
}
