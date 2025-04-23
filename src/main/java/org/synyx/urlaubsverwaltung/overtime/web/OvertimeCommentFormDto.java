package org.synyx.urlaubsverwaltung.overtime.web;

import java.io.Serializable;

public class OvertimeCommentFormDto implements Serializable {

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
