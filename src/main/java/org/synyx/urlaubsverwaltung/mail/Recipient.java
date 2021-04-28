package org.synyx.urlaubsverwaltung.mail;

public class Recipient {

    private String email;
    private String niceName;

    public Recipient(String email, String niceName) {
        this.email = email;
        this.niceName = niceName;
    }

    public String getEmail() {
        return email;
    }

    public String getNiceName() {
        return niceName;
    }
}
