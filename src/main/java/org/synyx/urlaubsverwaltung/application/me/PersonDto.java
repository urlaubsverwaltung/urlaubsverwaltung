package org.synyx.urlaubsverwaltung.application.me;

import java.util.Objects;

public final class PersonDto {
    private final String gravatarUrl;
    private final String niceName;
    private final String initials;

    PersonDto(
        String gravatarUrl,
        String niceName,
        String initials
    ) {
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
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PersonDto) obj;
        return Objects.equals(this.gravatarUrl, that.gravatarUrl) &&
            Objects.equals(this.niceName, that.niceName) &&
            Objects.equals(this.initials, that.initials);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gravatarUrl, niceName, initials);
    }

    @Override
    public String toString() {
        return "PersonDto[" +
            "gravatarUrl=" + gravatarUrl + ", " +
            "niceName=" + niceName + ", " +
            "initials=" + initials + ']';
    }

}
