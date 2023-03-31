package org.synyx.urlaubsverwaltung.person.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PersonPermissionsDto {

    private Integer id;
    private String niceName;
    private String gravatarURL;
    private String email;
    private Boolean isInactive;

    private List<PersonPermissionsRoleDto> permissions = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNiceName() {
        return niceName;
    }

    public void setNiceName(String niceName) {
        this.niceName = niceName;
    }

    public String getGravatarURL() {
        return gravatarURL;
    }

    public PersonPermissionsDto setGravatarURL(String gravatarURL) {
        this.gravatarURL = gravatarURL;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public PersonPermissionsDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public Boolean getIsInactive() {
        return isInactive;
    }

    public PersonPermissionsDto setIsInactive(Boolean isInactive) {
        this.isInactive = isInactive;
        return this;
    }

    public List<PersonPermissionsRoleDto> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PersonPermissionsRoleDto> permissions) {
        this.permissions = permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonPermissionsDto that = (PersonPermissionsDto) o;
        return id.equals(that.id) && permissions.equals(that.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, permissions);
    }
}
