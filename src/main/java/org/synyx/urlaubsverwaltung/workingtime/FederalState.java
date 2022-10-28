package org.synyx.urlaubsverwaltung.workingtime;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

/**
 * Enum representing the federal states of Germany. The information about the federal state is needed to check if a day
 * is a public holiday. This is the reason why the enum contains multiple entries for Bayern (there are areas with
 * different public holidays).
 */
public enum FederalState {

    NONE("none"),

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

    GREECE_GREECE("gr", "gr"),

    AUSTRIA_BURGENLAND("at", "b"),
    AUSTRIA_KAERNTEN("at", "k"),
    AUSTRIA_NIEDEROESTERREICH("at", "la"),
    AUSTRIA_OBEROESTERREICH("at", "ua"),
    AUSTRIA_SALZBURG("at", "s"),
    AUSTRIA_STEIERMARK("at", "st"),
    AUSTRIA_TIROL("at", "t"),
    AUSTRIA_VORARLBERG("at", "va"),
    AUSTRIA_WIEN("at", "w"),

    SWITZERLAND_AARGAU("ch", "ag"),
    SWITZERLAND_APPENZELL_INNERRHODEN("ch", "ai"),
    SWITZERLAND_APPENZELL_AUSSERRHODEN("ch", "ar"),
    SWITZERLAND_BERN("ch", "be"),
    SWITZERLAND_BASEL_LANDSCHAFT("ch", "bl"),
    SWITZERLAND_BASEL_STADT("ch", "bs"),
    SWITZERLAND_FREIBURG("ch", "fr"),
    SWITZERLAND_GENF("ch", "ge"),
    SWITZERLAND_GLARUS("ch", "gl"),
    SWITZERLAND_GRAUBUENDEN("ch", "gr"),
    SWITZERLAND_JURA("ch", "ju"),
    SWITZERLAND_LUZERN("ch", "lu"),
    SWITZERLAND_NEUENBURG("ch", "ne"),
    SWITZERLAND_NIDWALDEN("ch", "nw"),
    SWITZERLAND_OBWALDEN("ch", "ow"),
    SWITZERLAND_ST_GALLEN("ch", "sg"),
    SWITZERLAND_SCHAFFHAUSEN("ch", "sh"),
    SWITZERLAND_SOLOTHURN("ch", "so"),
    SWITZERLAND_SCHWYZ("ch", "sz"),
    SWITZERLAND_THURGAU("ch", "tg"),
    SWITZERLAND_TESSIN("ch", "ti"),
    SWITZERLAND_URI("ch", "ur"),
    SWITZERLAND_WAADT("ch", "vd"),
    SWITZERLAND_WALLIS("ch", "vs"),
    SWITZERLAND_ZUG("ch", "zg"),
    SWITZERLAND_ZUERICH("ch", "zh"),

    UNITED_KINGDOM_ALDERNEY("gb", "al"),
    UNITED_KINGDOM_ENGLAND("gb", "en"),
    UNITED_KINGDOM_GUERNSEY("gb", "gu"),
    UNITED_KINGDOM_ISLE_OF_MAN("gb", "im"),
    UNITED_KINGDOM_JERSEY("gb", "je"),
    UNITED_KINGDOM_NORTHERN_IRELAND("gb", "ni"),
    UNITED_KINGDOM_SCOTLAND("gb", "sc"),
    UNITED_KINGDOM_WALES("gb", "wa");

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

    /**
     * Returns a map of a country identifier and a list of federal states that this country has.
     * Does not return "none" (No publix holiday regulation) and the default
     *
     * @return Map of countries with a list of their federal states
     */
    public static Map<String, List<FederalState>> federalStatesTypesByCountry() {
        return Arrays.stream(values())
            .filter(federalState -> federalState != NONE)
            .collect(groupingBy(FederalState::getCountry));
    }
}
