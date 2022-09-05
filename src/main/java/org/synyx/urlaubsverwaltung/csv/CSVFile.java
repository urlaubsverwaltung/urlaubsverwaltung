package org.synyx.urlaubsverwaltung.csv;

import org.springframework.core.io.ByteArrayResource;

public class CSVFile {

    private final String fileName;
    private final ByteArrayResource resource;

    public CSVFile(String fileName, ByteArrayResource resource) {
        this.fileName = fileName;
        this.resource = resource;
    }

    public String getFileName() {
        return fileName;
    }

    public ByteArrayResource getResource() {
        return resource;
    }
}
