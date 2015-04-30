package org.synyx.urlaubsverwaltung.web.application;

/**
 * Daniel Hammann - <hammann@synyx.de>.
 */
public enum ApplicationForLeaveAction {

    CONFIRM("confirm"),
    CANCEL("cancel"),
    REJECT("reject");

    private String[] codes;

    ApplicationForLeaveAction(String... codes) {

        this.codes = codes;
    }

    public String[] getCodes() {

        return codes;
    }
}
