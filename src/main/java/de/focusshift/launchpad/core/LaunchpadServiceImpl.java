package de.focusshift.launchpad.core;

import de.focusshift.launchpad.api.LaunchpadAppUrlCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.stream.Collectors.toList;

class LaunchpadServiceImpl implements LaunchpadService {

    private static final Logger LOG = LoggerFactory.getLogger(lookup().lookupClass());

    private final LaunchpadConfigProperties appsProperties;
    private final LaunchpadAppUrlCustomizer appUrlCustomizer;

    LaunchpadServiceImpl(LaunchpadConfigProperties appsProperties, LaunchpadAppUrlCustomizer appUrlCustomizer) {
        this.appsProperties = appsProperties;
        this.appUrlCustomizer = appUrlCustomizer;
    }

    @Override
    public Launchpad getLaunchpad(Authentication authentication) {
        return new Launchpad(getApplications(authentication));
    }

    private List<App> getApplications(Authentication authentication) {
        return appsProperties.getApps()
            .stream()
            .map(this::toApp)
            .filter(Optional::isPresent)
            .flatMap(Optional::stream)
            .filter(app -> isAllowed(app, authentication))
            .collect(toList());
    }

    private boolean isAllowed(App app, Authentication authentication) {
        return app.getAuthority()
            .map(authority -> authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals)
            )
            .orElse(true);
    }

    private Optional<App> toApp(LaunchpadConfigProperties.App app) {
        return getAppUrl(app).map(url -> {
            final String defaultName = app.getName().get(appsProperties.getNameDefaultLocale());
            return new App(url, new AppName(defaultName, app.getName()), app.getIcon(), app.getAuthority());
        });
    }

    private Optional<URL> getAppUrl(LaunchpadConfigProperties.App app) {
        try {
            return Optional.of(appUrlCustomizer.customize(app.getUrl()));
        } catch (MalformedURLException e) {
            LOG.info("ignoring launchpad app: could not build URL for app={}", app, e);
            return Optional.empty();
        }
    }
}
