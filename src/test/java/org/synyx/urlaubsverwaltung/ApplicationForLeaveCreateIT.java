package org.synyx.urlaubsverwaltung;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.synyx.urlaubsverwaltung.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDate.now;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.period.WeekDay.FRIDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.MONDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.SATURDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.SUNDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.THURSDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.TUESDAY;
import static org.synyx.urlaubsverwaltung.period.WeekDay.WEDNESDAY;
import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.RECORD_FAILING;

@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ApplicationForLeaveCreateIT extends TestContainersBase {

    @LocalServerPort
    private int port;

    @Container
    private final BrowserWebDriverContainer<?> browserContainer = new BrowserWebDriverContainer<>()
        .withRecordingMode(RECORD_FAILING, new File("target"))
        .withCapabilities(new FirefoxOptions());

    @Autowired
    private PersonService personService;
    @Autowired
    private AccountInteractionService accountInteractionService;
    @Autowired
    private WorkingTimeService workingTimeService;

    @Test
    void title() {
        final Person person = createTestData();

        final RemoteWebDriver webDriver = browserContainer.getWebDriver();

        webDriver.get("http://172.17.0.1:" + port + "/login");
        assertThat(webDriver.getTitle()).isEqualTo("Login");

        webDriver.findElementById("username").sendKeys(person.getUsername());
        webDriver.findElementById("password").sendKeys("secret");
        webDriver.findElementByXPath("//button[@type='submit']").click();

        new WebDriverWait(webDriver, 20).until(visibilityOfElementLocated(By.id("application-new-link")));
        assertThat(webDriver.getTitle()).contains("Overview of Donald Bradley from");
        webDriver.findElementById("application-new-link").click();

        new WebDriverWait(webDriver, 20).until(visibilityOfElementLocated(By.id("from")));
        final String date = ofPattern("dd.MM.yyyy").format(now());
        assertThat(webDriver.getTitle()).isEqualTo("New vacation request");
        webDriver.findElementById("from").sendKeys(date);
        webDriver.findElementById("to").sendKeys(date);
        webDriver.findElementByXPath("//button[@type='submit']").click();

        new WebDriverWait(webDriver, 20).until(visibilityOfElementLocated(By.className("alert-success")));
        assertThat(webDriver.getTitle()).isEqualTo("Vacation request of Donald Bradley");
    }

    private Person createTestData() {
        final Person person = createPerson("dBradley", "Donald", "Bradley", "Donald.Bradley@example.org");
        person.setPassword("2f09520efd37e0add52eb78b19195ff9a07c07acbcfc9b61349be76da7a1bccfc60c9b80218d31ec");
        final Person savedPerson = personService.save(person);

        final int currentYear = LocalDate.now().getYear();
        final LocalDate validFrom = LocalDate.of(currentYear - 1, 1, 1);
        final List<Integer> workingDays = List.of(MONDAY.getDayOfWeek(), TUESDAY.getDayOfWeek(), WEDNESDAY.getDayOfWeek(), THURSDAY.getDayOfWeek(), FRIDAY.getDayOfWeek(), SATURDAY.getDayOfWeek(), SUNDAY.getDayOfWeek());
        workingTimeService.touch(workingDays, empty(), validFrom, savedPerson);

        final LocalDate firstDayOfYear = LocalDate.of(currentYear, JANUARY, 1);
        final LocalDate lastDayOfYear = LocalDate.of(currentYear, DECEMBER, 31);
        accountInteractionService.updateOrCreateHolidaysAccount(savedPerson, firstDayOfYear, lastDayOfYear, TEN, TEN, TEN, ZERO, null);

        return savedPerson;
    }
}
