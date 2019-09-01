package org.synyx.urlaubsverwaltung.feedback.web;

import org.synyx.urlaubsverwaltung.feedback.FeedbackType;

public class FeedbackViewDto {

    private FeedbackType type;
    private String text;

    public FeedbackType getType() {
        return type;
    }

    public void setType(FeedbackType type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
