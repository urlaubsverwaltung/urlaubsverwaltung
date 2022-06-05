package org.synyx.urlaubsverwaltung.web;

import java.util.List;

public class Asset {

    private String url;
    private List<String> dependencies;

    Asset() {}

    public String getUrl() {
        return url;
    }

    public Asset setUrl(String url) {
        this.url = url;
        return this;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public Asset setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
        return this;
    }
}

