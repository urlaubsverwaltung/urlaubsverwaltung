package org.synyx.urlaubsverwaltung.overtime.web;

public class OvertimeCommentPersonDto {

    private final String niceName;
    private final String gravatarUrl;

    public OvertimeCommentPersonDto(String niceName, String gravatarUrl) {
        this.niceName = niceName;
        this.gravatarUrl = gravatarUrl;
    }

    public String getNiceName() {
        return niceName;
    }

    public String getGravatarUrl() {
        return gravatarUrl;
    }
}
