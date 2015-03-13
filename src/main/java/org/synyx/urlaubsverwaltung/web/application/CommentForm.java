package org.synyx.urlaubsverwaltung.web.application;

/**
 * Represents a comment.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class CommentForm {

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
