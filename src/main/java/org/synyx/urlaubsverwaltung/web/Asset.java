package org.synyx.urlaubsverwaltung.web;

import java.util.List;
import java.util.Objects;

/**
 * Defines a frontend asset, most likely a JavaScript file.
 */
public final class Asset {

    private String url;
    private List<String> dependencies;

    Asset(String url, List<String> dependencies) {
        this.url = url;
        this.dependencies = dependencies;
    }

    private Asset() {
        // required for ObjectMapper
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Asset asset = (Asset) o;
        return Objects.equals(url, asset.url) && Objects.equals(dependencies, asset.dependencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, dependencies);
    }

    @Override
    public String toString() {
        return "Asset{" +
            "url='" + url + '\'' +
            ", dependencies=" + dependencies +
            '}';
    }
}
