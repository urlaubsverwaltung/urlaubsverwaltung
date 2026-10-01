package org.synyx.urlaubsverwaltung.mail;

import org.springframework.core.io.ByteArrayResource;

public record MailAttachment(String name, ByteArrayResource content) {
}
