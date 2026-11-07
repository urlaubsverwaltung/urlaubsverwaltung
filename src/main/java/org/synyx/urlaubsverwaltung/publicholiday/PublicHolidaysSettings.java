package org.synyx.urlaubsverwaltung.publicholiday;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import org.synyx.urlaubsverwaltung.workingtime.FederalState;

import java.io.Serializable;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG;

/**
 * Settings concerning working time of persons, like settings for public holidays.
 */
@Embeddable
public class PublicHolidaysSettings implements Serializable {

    /**
     * Defines the federal state of Germany to be able to check correctly if a day is a public holiday or not.
     */
    @Enumerated(STRING)
    private FederalState federalState = GERMANY_BADEN_WUERTTEMBERG;

    public FederalState getFederalState() {
        return federalState;
    }

    public void setFederalState(FederalState federalState) {
        this.federalState = federalState;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PublicHolidaysSettings that = (PublicHolidaysSettings) o;
        return federalState == that.federalState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(federalState);
    }
}
