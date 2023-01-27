package de.focusshift.launchpad.core;

import java.util.List;

public class Launchpad {

    private final List<App> apps;

    Launchpad(List<App> apps) {
        this.apps = apps;
    }

    public List<App> getApps() {
        return apps;
    }
}
