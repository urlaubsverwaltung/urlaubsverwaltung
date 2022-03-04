package org.synyx.urlaubsverwaltung.overtime.web;

import java.util.Objects;

public class OvertimeDetailPersonDto {

    private final Integer id;
    private final String email;
    private final String niceName;
    private final String gravatarUrl;

    OvertimeDetailPersonDto(Integer id, String email, String niceName, String gravatarUrl) {
        this.id = id;
        this.email = email;
        this.niceName = niceName;
        this.gravatarUrl = gravatarUrl;
    }

    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNiceName() {
        return niceName;
    }

    public String getGravatarUrl() {
        return gravatarUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OvertimeDetailPersonDto that = (OvertimeDetailPersonDto) o;
        return Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "OvertimeDetailPersonDto{" +
            "id=" + id +
            ", email='" + email + '\'' +
            ", niceName='" + niceName + '\'' +
            ", gravatarUrl='" + gravatarUrl + '\'' +
            '}';
    }
}
