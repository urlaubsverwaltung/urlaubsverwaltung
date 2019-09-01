package org.synyx.urlaubsverwaltung.feedback;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

public class Feedback {

    private FeedbackType type;
    private String text;

    private Feedback() {
        // for jackson reflection
    }

    public Feedback(FeedbackType type) {
        this.type = type;
    }

    public Feedback(FeedbackType type, String text) {
        this.type = type;
        this.text = text;
    }

    public FeedbackType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("type", getType())
            .append("text", getText())
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feedback feedback = (Feedback) o;
        return type == feedback.type &&
            Objects.equals(text, feedback.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, text);
    }
}
