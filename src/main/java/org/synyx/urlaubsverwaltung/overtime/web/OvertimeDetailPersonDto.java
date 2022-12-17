package org.synyx.urlaubsverwaltung.overtime.web;

import org.springframework.lang.Nullable;

import java.util.Objects;

public class OvertimeDetailPersonDto {

    private final Integer id;
    private final String email;
    private final String niceName;
    private final String gravatarURL;

    OvertimeDetailPersonDto(Integer id, @Nullable String email, String niceName, String gravatarURL) {
        this.id = id;
        this.email = email;
        this.niceName = niceName;
        this.gravatarURL = gravatarURL;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    public String getNiceName() {
        return niceName;
    }

    public String getGravatarURL() {
        return gravatarURL;
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
            ", gravatarUrl='" + gravatarURL + '\'' +
            '}';
    }
}
