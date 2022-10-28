package org.synyx.urlaubsverwaltung.mail;

import org.springframework.core.io.ByteArrayResource;

import java.util.Objects;

public final class MailAttachment {

    private final String name;
    private final ByteArrayResource content;

    MailAttachment(String name, ByteArrayResource content) {
        this.content = content;
        this.name = name;
    }

    public ByteArrayResource getContent() {
        return content;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MailAttachment that = (MailAttachment) o;
        return Objects.equals(name, that.name) && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, content);
    }
}
