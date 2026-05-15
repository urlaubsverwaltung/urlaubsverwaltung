package org.synyx.urlaubsverwaltung.overview;

final class PersonDto {

    private final String gravatarUrl;
    private final String niceName;
    private final String initials;

    PersonDto(String gravatarUrl, String niceName, String initials) {
        this.gravatarUrl = gravatarUrl;
        this.niceName = niceName;
        this.initials = initials;
    }

    public String getGravatarUrl() {
        return gravatarUrl;
    }

    public String getNiceName() {
        return niceName;
    }

    public String getInitials() {
        return initials;
    }

    @Override
    public String toString() {
        return "PersonDto{" +
            "gravatarUrl='" + gravatarUrl + '\'' +
            ", niceName='" + niceName + '\'' +
            ", initials='" + initials + '\'' +
            '}';
    }
}
