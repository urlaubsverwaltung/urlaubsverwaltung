package org.synyx.urlaubsverwaltung.overtime.web;

import java.time.Instant;

public class OvertimeCommentDto {

    private final OvertimeCommentPersonDto person;
    private final String action;
    private final Instant date;
    private final String text;

    OvertimeCommentDto(OvertimeCommentPersonDto person, String action, Instant date, String text) {
        this.person = person;
        this.action = action;
        this.date = date;
        this.text = text;
    }

    public OvertimeCommentPersonDto getPerson() {
        return person;
    }

    public String getAction() {
        return action;
    }

    public Instant getDate() {
        return date;
    }

    public String getText() {
        return text;
    }
}
