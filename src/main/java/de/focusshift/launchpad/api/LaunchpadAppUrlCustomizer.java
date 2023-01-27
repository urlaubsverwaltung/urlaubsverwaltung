package de.focusshift.launchpad.api;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Callback interface that can be implemented to customize the URL of an {@linkplain de.focusshift.launchpad.core.LaunchpadConfigProperties.App}.
 *
 * <p>
 * Example Configuration:
 *
 * <p>
 * <pre><code>
 * launchpad.apps[0].url=https://{tenantId}.example.org
 * launchpad.apps[0].name.de=Anwendung
 * launchpad.apps[0].name.en=App
 * </code></pre>
 *
 * <p>
 * Example Customizer:
 *
 * <p>
 * <pre><code>
 * LaunchpadAppUrlCustomizer launchpadAppUrlCustomizer() {
 *     return url -> new URL(url.replace("{tenantId}", "awesome-tenant-id"));
 * }
 * </code></pre>
 *
 */
@FunctionalInterface
public interface LaunchpadAppUrlCustomizer {

    URL customize(String url) throws MalformedURLException;
}
