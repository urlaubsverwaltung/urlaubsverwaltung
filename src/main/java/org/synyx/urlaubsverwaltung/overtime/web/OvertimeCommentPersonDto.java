package org.synyx.urlaubsverwaltung.overtime.web;

import java.util.Objects;

public class OvertimeCommentPersonDto {

    private final String niceName;
    private final String gravatarUrl;

    OvertimeCommentPersonDto(String niceName, String gravatarUrl) {
        this.niceName = niceName;
        this.gravatarUrl = gravatarUrl;
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
        OvertimeCommentPersonDto that = (OvertimeCommentPersonDto) o;
        return Objects.equals(niceName, that.niceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(niceName);
    }

    @Override
    public String toString() {
        return "OvertimeCommentPersonDto{" +
            "niceName='" + niceName + '\'' +
            ", gravatarUrl='" + gravatarUrl + '\'' +
            '}';
    }
}
