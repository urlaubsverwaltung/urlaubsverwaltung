package org.synyx.urlaubsverwaltung.mail;


import org.apache.commons.lang3.StringUtils;

public class RightPadder {
    static RightPadder getInstance() {
        return new RightPadder();
    }

    public String rightPad(final String str, final int size) {
        return StringUtils.rightPad(str, size);
    }
}
