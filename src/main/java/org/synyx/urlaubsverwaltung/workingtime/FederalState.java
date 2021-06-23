package org.synyx.urlaubsverwaltung.workingtime;

import java.util.Arrays;

/**
 * Enum representing the federal states of Germany. The information about the federal state is needed to check if a day
 * is a public holiday. This is the reason why the enum contains multiple entries for Bayern (there are areas with
 * different public holidays).
 */
public enum FederalState {

    BELGIUM("be"),
    GERMANY_BADEN_WUERTTEMBERG("de", "bw"),
    BAYERN("by"),
    BAYERN_MUENCHEN("by", "mu"),
    BAYERN_AUGSBURG("by", "ag"),
    BAYERN_WUERZBURG("by", "wu"),
    BAYERN_REGENSBURG("by", "re"),
    BAYERN_INGOLSTADT("by", "in"),
    BERLIN("b"),
    BRANDENBURG("bb"),
    BREMEN("hb"),
    HAMBURG("hh"),
    HESSEN("he"),
    MECKLENBURG_VORPOMMERN("mv"),
    NIEDERSACHSEN("ni"),
    NORDRHEIN_WESTFALEN("nw"),
    RHEINLAND_PFALZ("rp"),
    SAARLAND("sl"),
    SACHSEN("sn"),
    SACHSEN_ANHALT("st"),
    SCHLESWIG_HOLSTEIN("sh"),
    THUERINGEN("th");

    private final String[] codes;

    FederalState(String... codes) {
        this.codes = codes;
    }

    public String[] getCodes() {

        if (codes == null || codes.length <= 1) {
            return new String[0];
        }

        return Arrays.copyOfRange(codes, 1, codes.length);
    }

    public String getCountry() {

        if (codes == null || codes.length == 0) {
            return null;
        }

        return codes[0];
    }
}
