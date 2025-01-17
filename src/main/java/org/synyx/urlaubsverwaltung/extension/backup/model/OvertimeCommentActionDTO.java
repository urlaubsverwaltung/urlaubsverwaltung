package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction;

public enum OvertimeCommentActionDTO {
    CREATED, EDITED;

    public OvertimeCommentAction toOvertimeCommentAction() {
        return OvertimeCommentAction.valueOf(this.name());
    }
}
