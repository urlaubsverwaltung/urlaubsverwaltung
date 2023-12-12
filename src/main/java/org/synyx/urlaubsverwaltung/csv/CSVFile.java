package org.synyx.urlaubsverwaltung.csv;

import org.springframework.core.io.ByteArrayResource;

public record CSVFile(String fileName, ByteArrayResource resource) {
}
