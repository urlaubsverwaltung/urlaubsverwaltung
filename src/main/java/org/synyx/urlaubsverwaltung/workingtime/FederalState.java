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
    GERMANY_BERLIN("de", "be"),
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

    AUSTRIA_BURGENLAND("at", "1"),
    AUSTRIA_KAERNTEN("at", "2"),
    AUSTRIA_NIEDEROESTERREICH("at", "3"),
    AUSTRIA_OBEROESTERREICH("at", "4"),
    AUSTRIA_SALZBURG("at", "5"),
    AUSTRIA_STEIERMARK("at", "6"),
    AUSTRIA_TIROL("at", "7"),
    AUSTRIA_VORARLBERG("at", "8"),
    AUSTRIA_WIEN("at", "9"),

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
    UNITED_KINGDOM_WALES("gb", "wa"),

    MALTA("mt", "mt"),

    ITALY("it", "it"),

    CROATIA("hr", "hr"),

    SPAIN_ANDALUCIA("es", "an"),
    SPAIN_ARAGON("es", "ar"),
    SPAIN_CEUTA("es", "ce"),
    SPAIN_MELILLA("es", "ml"),
    SPAIN_CASTILE_AND_LEON("es", "cl"),
    SPAIN_CASTILE_LA_MANCHA("es", "cm"),
    SPAIN_CANARY_ISLANDS("es", "cn"),
    SPAIN_CATALONIA("es", "ct"),
    SPAIN_BARCELONA("es", "bcn"),
    SPAIN_EXTREMADURA("es", "ex"),
    SPAIN_GALICIA("es", "ga"),
    SPAIN_BALEARIC_ISLAND("es", "ib"),
    SPAIN_LA_RIOJA("es", "lo"),
    SPAIN_MADRID("es", "md"),
    SPAIN_MURCIA("es", "mc"),
    SPAIN_NAVARRA("es", "nc"),
    SPAIN_ASTURIAS("es", "as"),
    SPAIN_EUSKADI("es", "pv"),
    SPAIN_CANTABRIA("es", "cb"),
    SPAIN_VALENCIA("es", "vc"),

    NETHERLANDS("nl", "nl"),

    LITHUANIA("lt", "lt"),

    BELGIUM("be", "be"),

    POLAND("pl", "pl"),

    USA_MARYLAND("us", "md"),
    USA_VIRGINIA("us", "va"),
    USA_WASHINGTON_DC("us", "dc");

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
