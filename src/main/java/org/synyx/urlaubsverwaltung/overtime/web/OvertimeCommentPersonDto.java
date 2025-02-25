package org.synyx.urlaubsverwaltung.overtime.web;

import java.util.Objects;

public class OvertimeCommentPersonDto {

    private final Long id;
    private final String niceName;
    private final String initials;
    private final String gravatarUrl;

    OvertimeCommentPersonDto(Long id, String niceName, String initials, String gravatarUrl) {
        this.id = id;
        this.niceName = niceName;
        this.initials = initials;
        this.gravatarUrl = gravatarUrl;
    }

    public Long getId() {
        return id;
    }

    public String getNiceName() {
        return niceName;
    }

    public String getInitials() {
        return initials;
    }

    public String getGravatarUrl() {
        return gravatarUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OvertimeCommentPersonDto that = (OvertimeCommentPersonDto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OvertimeCommentPersonDto{" +
            "id=" + id +
            ", niceName='" + niceName + '\'' +
            ", gravatarUrl='" + gravatarUrl + '\'' +
            '}';
    }
}
