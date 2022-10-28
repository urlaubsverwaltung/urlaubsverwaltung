package org.synyx.urlaubsverwaltung.overtime.web;

import java.time.Instant;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OvertimeCommentDto that = (OvertimeCommentDto) o;
        return Objects.equals(person, that.person) && Objects.equals(action, that.action) && Objects.equals(date, that.date) && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(person, action, date, text);
    }

    @Override
    public String toString() {
        return "OvertimeCommentDto{" +
            "person=" + person +
            ", action='" + action + '\'' +
            ", date=" + date +
            ", text='" + text + '\'' +
            '}';
    }
}
