package de.focusshift.launchpad.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "launchpad")
class LaunchpadConfigProperties {

    @NotNull
    private Locale nameDefaultLocale;

    private List<App> apps = List.of();

    public Locale getNameDefaultLocale() {
        return nameDefaultLocale;
    }

    public void setNameDefaultLocale(Locale nameDefaultLocale) {
        this.nameDefaultLocale = nameDefaultLocale;
    }

    List<App> getApps() {
        return apps;
    }

    void setApps(List<App> apps) {
        this.apps = apps;
    }

    @Validated
    static class App {
        @NotNull
        private String url;

        @NotNull
        private Map<Locale, String> name;

        @NotEmpty
        private String icon;

        private String authority;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Map<Locale, String> getName() {
            return name;
        }

        public void setName(Map<Locale, String> name) {
            this.name = name;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getAuthority() {
            return authority;
        }

        public void setAuthority(String authority) {
            this.authority = authority;
        }
    }
}
