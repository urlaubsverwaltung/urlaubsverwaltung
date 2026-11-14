package org.synyx.urlaubsverwaltung.companyclosure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.time.Month.DECEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.period.DayLength.NOON;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest
@Transactional
class CompanyClosureServiceImplIT extends SingleTenantTestContainersBase {

    @Autowired
    private CompanyClosureService sut;
    @Autowired
    private CompanyClosureVersionRepository companyClosureVersionRepository;
    @Autowired
    private PersonService personService;

    @Test
    void defaultsExistAfterStartup() {
        assertThat(sut.getCompanyClosures(LocalDate.of(2026, DECEMBER, 1), LocalDate.of(2026, DECEMBER, 31)))
            .extracting(CompanyClosure::date, CompanyClosure::dayLength, closure -> closure.name(Locale.GERMAN), closure -> closure.name(Locale.ENGLISH))
            .containsExactly(
                tuple(LocalDate.of(2026, DECEMBER, 24), NOON, "Heiligabend", "Christmas Eve"),
                tuple(LocalDate.of(2026, DECEMBER, 31), NOON, "Silvester", "New Year's Eve"));
    }

    @Test
    void changePersistsANewVersion() {
        final Year year = Year.now();

        final Person office = personService.create("office", "Marlene", "Muster", "muster@example.org", List.of(), List.of(USER, OFFICE));

        sut.changeCompanyClosure(MonthDay.of(DECEMBER, 24), FULL, year.atDay(1), office);

        final LocalDate christmasEve = year.atMonthDay(MonthDay.of(DECEMBER, 24));
        final LocalDate lastYearsChristmasEve = year.minusYears(1).atMonthDay(MonthDay.of(DECEMBER, 24));
        assertThat(sut.getCompanyClosures(christmasEve, christmasEve)).extracting(CompanyClosure::dayLength).containsExactly(FULL);
        assertThat(sut.getCompanyClosures(lastYearsChristmasEve, lastYearsChristmasEve)).extracting(CompanyClosure::dayLength).containsExactly(NOON);
        assertThat(companyClosureVersionRepository.findByLastModifiedBy(office))
            .singleElement()
            .satisfies(version -> {
                assertThat(version.getValidFrom()).isEqualTo(year.atDay(1));
                assertThat(version.getLastModifiedAt()).isNotNull();
            });
    }

    @Test
    void deletingTheModifierKeepsTheVersion() {
        final Year year = Year.now();
        final Person office = personService.create("office", "Marlene", "Muster", "muster@example.org", List.of(), List.of(USER, OFFICE));
        final Person admin = personService.create("admin", "Max", "Muster", "max@example.org", List.of(), List.of(USER, OFFICE));
        sut.changeCompanyClosure(MonthDay.of(DECEMBER, 24), FULL, year.atDay(1), office);

        personService.delete(office.getIdAsPersonId(), admin.getIdAsPersonId());

        final LocalDate christmasEve = year.atMonthDay(MonthDay.of(DECEMBER, 24));
        assertThat(sut.getCompanyClosures(christmasEve, christmasEve)).extracting(CompanyClosure::dayLength).containsExactly(FULL);
        assertThat(companyClosureVersionRepository.findAll())
            .filteredOn(version -> year.atDay(1).equals(version.getValidFrom()))
            .singleElement()
            .satisfies(version -> {
                assertThat(version.getLastModifiedBy()).isNull();
                assertThat(version.getLastModifiedAt()).isNotNull();
            });
    }

    @Test
    void renamePersistsNamesPerLanguage() {
        sut.renameCompanyClosure(MonthDay.of(DECEMBER, 24), Map.of(Locale.GERMAN, "Weihnachtsruhe", Locale.forLanguageTag("el"), "Χριστούγεννα"));

        assertThat(sut.getCompanyClosureEntries())
            .filteredOn(entry -> entry.monthDay().equals(MonthDay.of(DECEMBER, 24)))
            .singleElement()
            .satisfies(entry -> {
                assertThat(entry.names()).isEqualTo(Map.of(Locale.GERMAN, "Weihnachtsruhe", Locale.forLanguageTag("el"), "Χριστούγεννα"));
                assertThat(entry.name(Locale.forLanguageTag("de-AT"))).isEqualTo("Weihnachtsruhe");
                assertThat(entry.name(Locale.ENGLISH)).isEqualTo("Company closure");
            });
    }
}
