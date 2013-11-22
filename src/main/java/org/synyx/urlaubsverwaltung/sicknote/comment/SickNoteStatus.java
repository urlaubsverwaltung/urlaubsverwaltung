package org.synyx.urlaubsverwaltung.sicknote.comment;

/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public enum SickNoteStatus {

    CREATED("sicknote.created"),
    EDITED("sicknote.edited"),
    CONVERTED_TO_VACATION("sicknote.converted"),
    COMMENTED("sicknote.commented");

    private String messageKey;

    SickNoteStatus(String messageKey) {

        this.messageKey = messageKey;
    }

    public String getMessageKey() {

        return messageKey;
    }
}
