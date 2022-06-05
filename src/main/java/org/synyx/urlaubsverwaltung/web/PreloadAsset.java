package org.synyx.urlaubsverwaltung.web;

/**
 * Describes a html preload link like <code>&lt;link rel="preload" as="script" href="/asset/foo.js" &gt;</code>
 */
public class PreloadAsset {

    private final String as;
    private final String href;

    public PreloadAsset(String as, String href) {
        this.as = as;
        this.href = href;
    }

    public String getAs() {
        return as;
    }

    public String getHref() {
        return href;
    }
}
