package org.synyx.urlaubsverwaltung.overtime.web;

public class OvertimeDetailPersonDto {

    private final Integer id;
    private final String email;
    private final String niceName;
    private final String gravatarUrl;

    public OvertimeDetailPersonDto(Integer id, String email, String niceName, String gravatarUrl) {
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
}
