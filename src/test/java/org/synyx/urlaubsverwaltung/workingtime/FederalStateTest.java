package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.AUSTRIA_BURGENLAND;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.AUSTRIA_KAERNTEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.AUSTRIA_NIEDEROESTERREICH;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.AUSTRIA_OBEROESTERREICH;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.AUSTRIA_SALZBURG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.AUSTRIA_STEIERMARK;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.AUSTRIA_TIROL;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.AUSTRIA_VORARLBERG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.AUSTRIA_WIEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BADEN_WUERTTEMBERG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BAYERN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BAYERN_AUGSBURG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BAYERN_INGOLSTADT;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BAYERN_MUENCHEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BAYERN_REGENSBURG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BAYERN_WUERZBURG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BERLIN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BRANDENBURG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BREMEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_HAMBURG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_HESSEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_MECKLENBURG_VORPOMMERN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_NIEDERSACHSEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_NORDRHEIN_WESTFALEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_RHEINLAND_PFALZ;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_SAARLAND;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_SACHSEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_SACHSEN_ANHALT;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_SCHLESWIG_HOLSTEIN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_THUERINGEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.NONE;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_AARGAU;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_APPENZELL_AUSSERRHODEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_APPENZELL_INNERRHODEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_BASEL_LANDSCHAFT;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_BASEL_STADT;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_BERN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_FREIBURG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_GENF;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_GLARUS;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_GRAUBUENDEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_JURA;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_LUZERN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_NEUENBURG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_NIDWALDEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_OBWALDEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_SCHAFFHAUSEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_SCHWYZ;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_SOLOTHURN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_ST_GALLEN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_TESSIN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_THURGAU;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_URI;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_WAADT;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_WALLIS;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_ZUERICH;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.SWITZERLAND_ZUG;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.UNITED_KINGDOM_ALDERNEY;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.UNITED_KINGDOM_ENGLAND;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.UNITED_KINGDOM_GUERNSEY;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.UNITED_KINGDOM_ISLE_OF_MAN;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.UNITED_KINGDOM_JERSEY;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.UNITED_KINGDOM_NORTHERN_IRELAND;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.UNITED_KINGDOM_SCOTLAND;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.UNITED_KINGDOM_WALES;

/**
 * Unit test for {@link FederalState}.
 */
class FederalStateTest {

    private static Stream<Arguments> provideFederalStateCode() {
        return Stream.of(
            Arguments.of(NONE, "none", null),
            Arguments.of(GERMANY_BADEN_WUERTTEMBERG, "bw", null),
            Arguments.of(GERMANY_BAYERN, "by", null),
            Arguments.of(GERMANY_BAYERN_MUENCHEN, "by", "mu"),
            Arguments.of(GERMANY_BAYERN_AUGSBURG, "by", "ag"),
            Arguments.of(GERMANY_BAYERN_WUERZBURG, "by", "wu"),
            Arguments.of(GERMANY_BAYERN_REGENSBURG, "by", "re"),
            Arguments.of(GERMANY_BAYERN_INGOLSTADT, "by", "in"),
            Arguments.of(GERMANY_BERLIN, "b", null),
            Arguments.of(GERMANY_BRANDENBURG, "bb", null),
            Arguments.of(GERMANY_BREMEN, "hb", null),
            Arguments.of(GERMANY_HAMBURG, "hh", null),
            Arguments.of(GERMANY_HESSEN, "he", null),
            Arguments.of(GERMANY_MECKLENBURG_VORPOMMERN, "mv", null),
            Arguments.of(GERMANY_NIEDERSACHSEN, "ni", null),
            Arguments.of(GERMANY_NORDRHEIN_WESTFALEN, "nw", null),
            Arguments.of(GERMANY_RHEINLAND_PFALZ, "rp", null),
            Arguments.of(GERMANY_SAARLAND, "sl", null),
            Arguments.of(GERMANY_SACHSEN, "sn", null),
            Arguments.of(GERMANY_SACHSEN_ANHALT, "st", null),
            Arguments.of(GERMANY_SCHLESWIG_HOLSTEIN, "sh", null),
            Arguments.of(GERMANY_THUERINGEN, "th", null),

            Arguments.of(AUSTRIA_BURGENLAND, "b", null),
            Arguments.of(AUSTRIA_KAERNTEN, "k", null),
            Arguments.of(AUSTRIA_NIEDEROESTERREICH, "la", null),
            Arguments.of(AUSTRIA_OBEROESTERREICH, "ua", null),
            Arguments.of(AUSTRIA_SALZBURG, "s", null),
            Arguments.of(AUSTRIA_STEIERMARK, "st", null),
            Arguments.of(AUSTRIA_TIROL, "t", null),
            Arguments.of(AUSTRIA_VORARLBERG, "va", null),
            Arguments.of(AUSTRIA_WIEN, "w", null),

            Arguments.of(SWITZERLAND_AARGAU, "ag", null),
            Arguments.of(SWITZERLAND_APPENZELL_INNERRHODEN, "ai", null),
            Arguments.of(SWITZERLAND_APPENZELL_AUSSERRHODEN, "ar", null),
            Arguments.of(SWITZERLAND_BERN, "be", null),
            Arguments.of(SWITZERLAND_BASEL_LANDSCHAFT, "bl", null),
            Arguments.of(SWITZERLAND_BASEL_STADT, "bs", null),
            Arguments.of(SWITZERLAND_FREIBURG, "fr", null),
            Arguments.of(SWITZERLAND_GENF, "ge", null),
            Arguments.of(SWITZERLAND_GLARUS, "gl", null),
            Arguments.of(SWITZERLAND_GRAUBUENDEN, "gr", null),
            Arguments.of(SWITZERLAND_JURA, "ju", null),
            Arguments.of(SWITZERLAND_LUZERN, "lu", null),
            Arguments.of(SWITZERLAND_NEUENBURG, "ne", null),
            Arguments.of(SWITZERLAND_NIDWALDEN, "nw", null),
            Arguments.of(SWITZERLAND_OBWALDEN, "ow", null),
            Arguments.of(SWITZERLAND_ST_GALLEN, "sg", null),
            Arguments.of(SWITZERLAND_SCHAFFHAUSEN, "sh", null),
            Arguments.of(SWITZERLAND_SOLOTHURN, "so", null),
            Arguments.of(SWITZERLAND_SCHWYZ, "sz", null),
            Arguments.of(SWITZERLAND_THURGAU, "tg", null),
            Arguments.of(SWITZERLAND_TESSIN, "ti", null),
            Arguments.of(SWITZERLAND_URI, "ur", null),
            Arguments.of(SWITZERLAND_WAADT, "vd", null),
            Arguments.of(SWITZERLAND_WALLIS, "vs", null),
            Arguments.of(SWITZERLAND_ZUG, "zg", null),
            Arguments.of(SWITZERLAND_ZUERICH, "zh", null),

            Arguments.of(UNITED_KINGDOM_ALDERNEY, "al", null),
            Arguments.of(UNITED_KINGDOM_ENGLAND, "en", null),
            Arguments.of(UNITED_KINGDOM_GUERNSEY, "gu", null),
            Arguments.of(UNITED_KINGDOM_ISLE_OF_MAN, "im", null),
            Arguments.of(UNITED_KINGDOM_JERSEY, "je", null),
            Arguments.of(UNITED_KINGDOM_NORTHERN_IRELAND, "ni", null),
            Arguments.of(UNITED_KINGDOM_SCOTLAND, "sc", null),
            Arguments.of(UNITED_KINGDOM_WALES, "wa", null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideFederalStateCode")
    void ensureCorrectCode(FederalState federalState, String region, String subregion) {
        final String[] codes = federalState.getCodes();
        if (federalState == NONE) {
            assertThat(codes)
                .isEmpty();
        } else if (subregion == null) {
            assertThat(codes)
                .hasSize(1)
                .contains(region);
        } else {
            assertThat(codes)
                .hasSize(2)
                .contains(region, subregion);
        }
    }

    private static Stream<Arguments> provideFederalStateCountry() {
        return Stream.of(
            Arguments.of(NONE, "none"),
            Arguments.of(GERMANY_BADEN_WUERTTEMBERG, "de"),
            Arguments.of(GERMANY_BAYERN, "de"),
            Arguments.of(GERMANY_BAYERN_MUENCHEN, "de"),
            Arguments.of(GERMANY_BAYERN_AUGSBURG, "de"),
            Arguments.of(GERMANY_BAYERN_WUERZBURG, "de"),
            Arguments.of(GERMANY_BAYERN_REGENSBURG, "de"),
            Arguments.of(GERMANY_BAYERN_INGOLSTADT, "de"),
            Arguments.of(GERMANY_BERLIN, "de"),
            Arguments.of(GERMANY_BRANDENBURG, "de"),
            Arguments.of(GERMANY_BREMEN, "de"),
            Arguments.of(GERMANY_HAMBURG, "de"),
            Arguments.of(GERMANY_HESSEN, "de"),
            Arguments.of(GERMANY_MECKLENBURG_VORPOMMERN, "de"),
            Arguments.of(GERMANY_NIEDERSACHSEN, "de"),
            Arguments.of(GERMANY_NORDRHEIN_WESTFALEN, "de"),
            Arguments.of(GERMANY_RHEINLAND_PFALZ, "de"),
            Arguments.of(GERMANY_SAARLAND, "de"),
            Arguments.of(GERMANY_SACHSEN, "de"),
            Arguments.of(GERMANY_SACHSEN_ANHALT, "de"),
            Arguments.of(GERMANY_SCHLESWIG_HOLSTEIN, "de"),
            Arguments.of(GERMANY_THUERINGEN, "de"),

            Arguments.of(AUSTRIA_BURGENLAND, "at"),
            Arguments.of(AUSTRIA_KAERNTEN, "at"),
            Arguments.of(AUSTRIA_NIEDEROESTERREICH, "at"),
            Arguments.of(AUSTRIA_OBEROESTERREICH, "at"),
            Arguments.of(AUSTRIA_SALZBURG, "at"),
            Arguments.of(AUSTRIA_STEIERMARK, "at"),
            Arguments.of(AUSTRIA_TIROL, "at"),
            Arguments.of(AUSTRIA_VORARLBERG, "at"),
            Arguments.of(AUSTRIA_WIEN, "at"),

            Arguments.of(SWITZERLAND_AARGAU, "ch"),
            Arguments.of(SWITZERLAND_APPENZELL_INNERRHODEN, "ch"),
            Arguments.of(SWITZERLAND_APPENZELL_AUSSERRHODEN, "ch"),
            Arguments.of(SWITZERLAND_BERN, "ch"),
            Arguments.of(SWITZERLAND_BASEL_LANDSCHAFT, "ch"),
            Arguments.of(SWITZERLAND_BASEL_STADT, "ch"),
            Arguments.of(SWITZERLAND_FREIBURG, "ch"),
            Arguments.of(SWITZERLAND_GENF, "ch"),
            Arguments.of(SWITZERLAND_GLARUS, "ch"),
            Arguments.of(SWITZERLAND_GRAUBUENDEN, "ch"),
            Arguments.of(SWITZERLAND_JURA, "ch"),
            Arguments.of(SWITZERLAND_LUZERN, "ch"),
            Arguments.of(SWITZERLAND_NEUENBURG, "ch"),
            Arguments.of(SWITZERLAND_NIDWALDEN, "ch"),
            Arguments.of(SWITZERLAND_OBWALDEN, "ch"),
            Arguments.of(SWITZERLAND_ST_GALLEN, "ch"),
            Arguments.of(SWITZERLAND_SCHAFFHAUSEN, "ch"),
            Arguments.of(SWITZERLAND_SOLOTHURN, "ch"),
            Arguments.of(SWITZERLAND_SCHWYZ, "ch"),
            Arguments.of(SWITZERLAND_THURGAU, "ch"),
            Arguments.of(SWITZERLAND_TESSIN, "ch"),
            Arguments.of(SWITZERLAND_URI, "ch"),
            Arguments.of(SWITZERLAND_WAADT, "ch"),
            Arguments.of(SWITZERLAND_WALLIS, "ch"),
            Arguments.of(SWITZERLAND_ZUG, "ch"),
            Arguments.of(SWITZERLAND_ZUERICH, "ch"),

            Arguments.of(UNITED_KINGDOM_ALDERNEY, "gb"),
            Arguments.of(UNITED_KINGDOM_ENGLAND, "gb"),
            Arguments.of(UNITED_KINGDOM_GUERNSEY, "gb"),
            Arguments.of(UNITED_KINGDOM_ISLE_OF_MAN, "gb"),
            Arguments.of(UNITED_KINGDOM_JERSEY, "gb"),
            Arguments.of(UNITED_KINGDOM_NORTHERN_IRELAND, "gb"),
            Arguments.of(UNITED_KINGDOM_SCOTLAND, "gb"),
            Arguments.of(UNITED_KINGDOM_WALES, "gb")
        );
    }

    @ParameterizedTest
    @MethodSource("provideFederalStateCountry")
    void ensureCorrectCountry(FederalState federalState, String country) {
        assertThat(federalState.getCountry()).isEqualTo(country);
    }

    @Test
    void ensureCorrectFederalStatesByCountry() {
        final Map<String, List<FederalState>> federalStatesTypesByCountry = FederalState.federalStatesTypesByCountry();
        final List<FederalState> germanyFederalStates = Arrays.stream(FederalState.values()).filter(federalState -> "de".equals(federalState.getCountry())).collect(toList());
        final List<FederalState> austriaFederalStates = Arrays.stream(FederalState.values()).filter(federalState -> "at".equals(federalState.getCountry())).collect(toList());
        final List<FederalState> switzerlandFederalStates = Arrays.stream(FederalState.values()).filter(federalState -> "ch".equals(federalState.getCountry())).collect(toList());
        final List<FederalState> ukFederalStates = Arrays.stream(FederalState.values()).filter(federalState -> "gb".equals(federalState.getCountry())).collect(toList());
        assertThat(federalStatesTypesByCountry).hasSize(4)
            .contains(entry("de", germanyFederalStates), entry("at", austriaFederalStates), entry("ch", switzerlandFederalStates), entry("gb", ukFederalStates));
    }
}
