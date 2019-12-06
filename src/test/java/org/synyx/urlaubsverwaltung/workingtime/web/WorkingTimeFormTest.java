package org.synyx.urlaubsverwaltung.workingtime.web;


import org.junit.Test;
import org.synyx.urlaubsverwaltung.settings.FederalState;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkingTimeFormTest {

    private static final LocalDate VALID_FROM = LocalDate.now().minusDays(10);
    private static final List<Integer> WORKING_DAYS = Arrays.asList(3, 4, 5, 7);
    private static final FederalState FEDERAL_STATE = FederalState.BERLIN;

    @Test
    public void ensureEqualsUsesCorrectAttributes() {

        WorkingTimeForm form1 = new WorkingTimeForm();
        form1.setValidFrom(VALID_FROM);
        form1.setWorkingDays(WORKING_DAYS);
        form1.setFederalState(FEDERAL_STATE);

        WorkingTimeForm form2 = new WorkingTimeForm();
        form2.setValidFrom(VALID_FROM);
        form2.setWorkingDays(WORKING_DAYS);
        form2.setFederalState(FEDERAL_STATE);

        // equals if same attributes
        assertThat(form1.equals(form2)).isTrue();

        // not equal if differ in attribute validFrom
        form2.setValidFrom(VALID_FROM.minusDays(50));
        assertThat(form1.equals(form2)).isFalse();
        form2.setValidFrom(VALID_FROM);

        // not equal if differ in attribute workingDays
        form2.setWorkingDays(Arrays.asList(1, 2));
        assertThat(form1.equals(form2)).isFalse();
        form2.setWorkingDays(WORKING_DAYS);

        // not equal if differ in attribute federalState
        form2.setFederalState(FederalState.BADEN_WUERTTEMBERG);
        assertThat(form1.equals(form2)).isFalse();
        form2.setFederalState(FEDERAL_STATE);
    }

    @Test
    public void ensureHashCodeUsesCorrectAttribute() {

        WorkingTimeForm form1 = new WorkingTimeForm();
        form1.setValidFrom(VALID_FROM);
        form1.setWorkingDays(WORKING_DAYS);
        form1.setFederalState(FEDERAL_STATE);

        WorkingTimeForm form2 = new WorkingTimeForm();
        form2.setValidFrom(VALID_FROM);
        form2.setWorkingDays(WORKING_DAYS);
        form2.setFederalState(FEDERAL_STATE);

        // same hashCode if same attributes
        assertThat(form1.hashCode()).isEqualTo(form2.hashCode());

        // not same hashCode if differ in attribute validFrom
        form2.setValidFrom(VALID_FROM.minusDays(50));
        assertThat(form1.hashCode()).isNotEqualTo(form2.hashCode());
        form2.setValidFrom(VALID_FROM);

        // not same hashCode if differ in attribute workingDays
        form2.setWorkingDays(Arrays.asList(1, 2));
        assertThat(form1.hashCode()).isNotEqualTo(form2.hashCode());
        form2.setWorkingDays(WORKING_DAYS);

        // not same hashCode if differ in attribute federalState
        form2.setFederalState(FederalState.BADEN_WUERTTEMBERG);
        assertThat(form1.hashCode()).isNotEqualTo(form2.hashCode());
        form2.setFederalState(FEDERAL_STATE);
    }
}
