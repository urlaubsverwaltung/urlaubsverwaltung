package de.focusshift.launchpad.core;

import java.net.URL;
import java.util.Optional;

public class App {

    private final URL url;
    private final AppName appName;
    private final String icon;
    private final String authority;

    App(URL url, AppName appName, String icon) {
        this(url, appName, icon, null);
    }

    App(URL url, AppName appName, String icon, String authority) {
        this.url = url;
        this.appName = appName;
        this.icon = icon;
        this.authority = authority;
    }

    public URL getUrl() {
        return url;
    }

    public AppName getAppName() {
        return appName;
    }

    public String getIcon() {
        return icon;
    }

    public Optional<String> getAuthority() {
        return Optional.ofNullable(authority);
    }
}
