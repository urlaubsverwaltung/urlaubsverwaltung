package org.synyx.urlaubsverwaltung.web.html;

/**
 * Describes a html preload link like <code>&lt;link rel="preload" as="script" href="/asset/foo.js" &gt;</code>
 */
public record PreloadLink(String as, String href) {
}
