package org.synyx.urlaubsverwaltung.settings;

import org.junit.Assert;
import org.junit.Test;


/**
 * Unit test for {@link FederalState}.
 */
public class FederalStateTest {

    @Test
    public void ensureCorrectCodeForBerlin() {

        String[] codes = FederalState.BERLIN.getCodes();

        Assert.assertEquals("Wrong number of codes", 1, codes.length);
    }


    @Test
    public void ensureCorrectCodeForBadenWuerttemberg() {

        String[] codes = FederalState.BADEN_WUERTTEMBERG.getCodes();

        Assert.assertEquals("Wrong number of codes", 1, codes.length);
        Assert.assertEquals("Wrong code", "bw", codes[0]);
    }


    @Test
    public void ensureCorrectCodeForBayernMuenchen() {

        String[] codes = FederalState.BAYERN_MUENCHEN.getCodes();

        Assert.assertEquals("Wrong number of codes", 2, codes.length);
        Assert.assertEquals("Wrong code", "by", codes[0]);
        Assert.assertEquals("Wrong code", "mu", codes[1]);
    }
}
