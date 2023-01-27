package de.focusshift.launchpad.core;

import java.util.List;

class LaunchpadDto {

    private final List<AppDto> apps;

    LaunchpadDto(List<AppDto> apps) {
        this.apps = apps;
    }

    public List<AppDto> getApps() {
        return apps;
    }
}
