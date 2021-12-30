package org.synyx.urlaubsverwaltung.workingtime;


import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG;

class WorkingTimeFormTest {

    private static final LocalDate VALID_FROM = LocalDate.now().minusDays(10);
    private static final List<Integer> WORKING_DAYS = asList(3, 4, 5, 7);
    private static final FederalState FEDERAL_STATE = FederalState.GERMANY_BERLIN;

    @Test
    void ensureEqualsUsesCorrectAttributes() {

        WorkingTimeForm form1 = new WorkingTimeForm();
        form1.setValidFrom(VALID_FROM);
        form1.setWorkingDays(WORKING_DAYS);
        form1.setFederalState(FEDERAL_STATE);

        WorkingTimeForm form2 = new WorkingTimeForm();
        form2.setValidFrom(VALID_FROM);
        form2.setWorkingDays(WORKING_DAYS);
        form2.setFederalState(FEDERAL_STATE);

        // equals if same attributes
        assertThat(form1).isEqualTo(form2);

        // not equal if differ in attribute validFrom
        form2.setValidFrom(VALID_FROM.minusDays(50));
        assertThat(form1).isNotEqualTo(form2);
        form2.setValidFrom(VALID_FROM);

        // not equal if differ in attribute workingDays
        form2.setWorkingDays(asList(1, 2));
        assertThat(form1).isNotEqualTo(form2);
        form2.setWorkingDays(WORKING_DAYS);

        // not equal if differ in attribute federalState
        form2.setFederalState(GERMANY_BADEN_WUERTTEMBERG);
        assertThat(form1).isNotEqualTo(form2);
        form2.setFederalState(FEDERAL_STATE);
    }

    @Test
    void ensureHashCodeUsesCorrectAttribute() {

        final WorkingTimeForm form1 = new WorkingTimeForm();
        form1.setValidFrom(VALID_FROM);
        form1.setWorkingDays(WORKING_DAYS);
        form1.setFederalState(FEDERAL_STATE);

        final WorkingTimeForm form2 = new WorkingTimeForm();
        form2.setValidFrom(VALID_FROM);
        form2.setWorkingDays(WORKING_DAYS);
        form2.setFederalState(FEDERAL_STATE);

        // same hashCode if same attributes
        assertThat(form1).hasSameHashCodeAs(form2);

        // not same hashCode if differ in attribute validFrom
        form2.setValidFrom(VALID_FROM.minusDays(50));
        assertThat(form1.hashCode()).isNotEqualTo(form2.hashCode());
        form2.setValidFrom(VALID_FROM);

        // not same hashCode if differ in attribute workingDays
        form2.setWorkingDays(asList(1, 2));
        assertThat(form1.hashCode()).isNotEqualTo(form2.hashCode());
        form2.setWorkingDays(WORKING_DAYS);

        // not same hashCode if differ in attribute federalState
        form2.setFederalState(GERMANY_BADEN_WUERTTEMBERG);
        assertThat(form1.hashCode()).isNotEqualTo(form2.hashCode());
        form2.setFederalState(FEDERAL_STATE);
    }

    @Test
    void ensureEmptyValidFromIsoValue() {

        final WorkingTimeForm workingTimeForm = new WorkingTimeForm();
        workingTimeForm.setValidFrom(null);

        assertThat(workingTimeForm.getValidFromIsoValue()).isEmpty();
    }

    @Test
    void ensureValidFromIsoValue() {

        final WorkingTimeForm workingTimeForm = new WorkingTimeForm();
        workingTimeForm.setValidFrom(LocalDate.parse("2020-10-30"));

        assertThat(workingTimeForm.getValidFromIsoValue()).isEqualTo("2020-10-30");
    }
}
