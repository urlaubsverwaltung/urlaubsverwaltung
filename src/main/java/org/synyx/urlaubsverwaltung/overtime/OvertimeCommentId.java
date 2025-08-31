package org.synyx.urlaubsverwaltung.overtime;

import static org.springframework.util.Assert.notNull;

public record OvertimeCommentId(Long value) {

    public OvertimeCommentId {
        notNull(value, "OvertimeCommentId value must not be null");
    }
}
