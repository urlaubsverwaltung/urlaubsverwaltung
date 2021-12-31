package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

/**
 * Unit test for {@link FederalState}.
 */
class FederalStateTest {

    private static Stream<Arguments> provideFederalStateCode() {
        return Stream.of(
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
            Arguments.of(AUSTRIA_WIEN, "w", null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideFederalStateCode")
    void ensureCorrectCode(FederalState federalState, String region, String subregion) {
        final String[] codes = federalState.getCodes();
        if (subregion == null) {
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
            Arguments.of(AUSTRIA_WIEN, "at")
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
        final List<FederalState> germanyFederalStates = Arrays.stream(FederalState.values()).filter(federalState -> "de".equals(federalState.getCountry())).collect(Collectors.toList());
        final List<FederalState> austriaFederalStates = Arrays.stream(FederalState.values()).filter(federalState -> "at".equals(federalState.getCountry())).collect(Collectors.toList());
        assertThat(federalStatesTypesByCountry).hasSize(2)
            .contains(entry("de", germanyFederalStates), entry("at", austriaFederalStates));
    }
}
