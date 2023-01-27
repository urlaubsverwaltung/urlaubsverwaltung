package de.focusshift.launchpad.core;

public class AppDto {

    private final String url;
    private final String name;
    private final String icon;

    AppDto(String url, String name, String icon) {
        this.url = url;
        this.name = name;
        this.icon = icon;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }
}
