package org.synyx.urlaubsverwaltung.sicknote.comment;

import java.io.Serializable;

public class SickNoteCommentFormDto implements Serializable {

    private String text;
    private boolean isMandatory;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public void setMandatory(boolean isMandatory) {
        this.isMandatory = isMandatory;
    }
}
