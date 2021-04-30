package org.synyx.urlaubsverwaltung.mail;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipient recipient = (Recipient) o;
        return Objects.equals(email, recipient.email) && Objects.equals(niceName, recipient.niceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, niceName);
    }

    @Override
    public String toString() {
        return "Recipient{" +
            "email='" + email + '\'' +
            ", niceName='" + niceName + '\'' +
            '}';
    }
}
