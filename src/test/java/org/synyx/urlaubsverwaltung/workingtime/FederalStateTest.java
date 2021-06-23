package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit test for {@link FederalState}.
 */
class FederalStateTest {

    @Test
    void ensureCorrectCodeForBerlin() {

        String[] codes = FederalState.BERLIN.getCodes();

        assertThat(codes).hasSize(1);
        assertThat(codes[0]).isEqualTo("b");
    }


    @Test
    void ensureCorrectCodeForBadenWuerttemberg() {

        String[] codes = FederalState.GERMANY_BADEN_WUERTTEMBERG.getCodes();

        assertThat(codes).hasSize(1);
        assertThat(codes[0]).isEqualTo("bw");
    }


    @Test
    void ensureCorrectCodeForBayernMuenchen() {

        String[] codes = FederalState.BAYERN_MUENCHEN.getCodes();

        assertThat(codes).hasSize(2);
        assertThat(codes[0]).isEqualTo("by");
        assertThat(codes[1]).isEqualTo("mu");
    }
}
