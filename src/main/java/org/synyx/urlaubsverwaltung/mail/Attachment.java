package org.synyx.urlaubsverwaltung.mail;

import java.io.File;
import java.util.Objects;

public final class Attachment {

    private final String name;
    private final File file;

    public Attachment(String name, File file) {
        this.file = file;
        this.name = name;
    }

    public File getFile() {
        return file;
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
        Attachment that = (Attachment) o;
        return Objects.equals(name, that.name) && Objects.equals(file, that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, file);
    }
}
