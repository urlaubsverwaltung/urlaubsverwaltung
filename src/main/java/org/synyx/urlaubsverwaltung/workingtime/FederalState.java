package org.synyx.urlaubsverwaltung.workingtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enum representing the federal states of Germany. The information about the federal state is needed to check if a day
 * is a public holiday. This is the reason why the enum contains multiple entries for Bayern (there are areas with
 * different public holidays).
 */
public enum FederalState {

    GERMANY_BADEN_WUERTTEMBERG("de", "bw"),
    GERMANY_BAYERN("de", "by"),
    GERMANY_BAYERN_MUENCHEN("de", "by", "mu"),
    GERMANY_BAYERN_AUGSBURG("de", "by", "ag"),
    GERMANY_BAYERN_WUERZBURG("de", "by", "wu"),
    GERMANY_BAYERN_REGENSBURG("de", "by", "re"),
    GERMANY_BAYERN_INGOLSTADT("de", "by", "in"),
    GERMANY_BERLIN("de", "b"),
    GERMANY_BRANDENBURG("de", "bb"),
    GERMANY_BREMEN("de", "hb"),
    GERMANY_HAMBURG("de", "hh"),
    GERMANY_HESSEN("de", "he"),
    GERMANY_MECKLENBURG_VORPOMMERN("de", "mv"),
    GERMANY_NIEDERSACHSEN("de", "ni"),
    GERMANY_NORDRHEIN_WESTFALEN("de", "nw"),
    GERMANY_RHEINLAND_PFALZ("de", "rp"),
    GERMANY_SAARLAND("de", "sl"),
    GERMANY_SACHSEN("de", "sn"),
    GERMANY_SACHSEN_ANHALT("de", "st"),
    GERMANY_SCHLESWIG_HOLSTEIN("de", "sh"),
    GERMANY_THUERINGEN("de", "th"),

    AUSTRIA_BURGENLAND("at", "b"),
    AUSTRIA_KAERNTEN("at", "k"),
    AUSTRIA_NIEDEROESTERREICH("at", "la"),
    AUSTRIA_OBEROESTERREICH("at", "ua"),
    AUSTRIA_SALZBURG("at", "s"),
    AUSTRIA_STEIERMARK("at", "st"),
    AUSTRIA_TIROL("at", "t"),
    AUSTRIA_VORARLBERG("at", "va"),
    AUSTRIA_WIEN("at", "w");

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

    public static Map<String, List<FederalState>> federalStatesTypesByCountry() {

        final Map<String, List<FederalState>> federalStatesTypesByCountry = new HashMap<>();

        Arrays.stream(values()).forEach(federalState ->
            federalStatesTypesByCountry.computeIfAbsent(federalState.getCountry(), country -> new ArrayList<>()).add(federalState));

        return federalStatesTypesByCountry;
    }
}
