package org.synyx.urlaubsverwaltung.web.validator;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.ui.Model;

import org.springframework.validation.Errors;

import org.synyx.urlaubsverwaltung.core.application.domain.Comment;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.web.application.AppForm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * @author  Aljona Murygina
 */
public class ApplicationValidatorTest {

    private ApplicationValidator instance;
    private AppForm app;
    Errors errors = Mockito.mock(Errors.class);
    private PropertiesValidator propValidator = Mockito.mock(PropertiesValidator.class);

    public ApplicationValidatorTest() throws Exception {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() throws Exception {

        instance = new ApplicationValidator(propValidator);
        app = new AppForm();
        Mockito.reset(errors);
    }


    @After
    public void tearDown() {
    }


    /**
     * Test of supports method, of class ApplicationValidator.
     */
    @Test
    public void testSupports() {

        boolean returnValue;

        returnValue = instance.supports(null);
        assertFalse(returnValue);

        returnValue = instance.supports(Person.class);
        assertFalse(returnValue);

        returnValue = instance.supports(AppForm.class);
        assertTrue(returnValue);
    }


    /**
     * Test of validate method, of class ApplicationValidator.
     */
    @Test
    public void testValidate() {

        app.setVacationType(VacationType.HOLIDAY);
        app.setHowLong(DayLength.FULL);

        // if mandatory fields are empty

        // if date fields are null
        app.setEndDate(null);
        instance.validate(app, errors);
        Mockito.verify(errors).rejectValue("endDate", "error.mandatory.field");
        Mockito.reset(errors);

        app.setStartDate(null);
        instance.validate(app, errors);
        Mockito.verify(errors).rejectValue("startDate", "error.mandatory.field");
        Mockito.reset(errors);

        app.setHowLong(DayLength.MORNING);
        app.setStartDateHalf(null);
        instance.validate(app, errors);
        Mockito.verify(errors).rejectValue("startDateHalf", "error.mandatory.field");
        Mockito.reset(errors);

        // if field reason is empty

        app.setVacationType(VacationType.OVERTIME);
        app.setReason(null);
        instance.validate(app, errors);
        Mockito.verify(errors).rejectValue("reason", "error.mandatory.field");
        Mockito.reset(errors);

        app.setReason("");
        instance.validate(app, errors);
        Mockito.verify(errors).rejectValue("reason", "error.mandatory.field");
        Mockito.reset(errors);

        // if from > to
        app.setHowLong(DayLength.FULL);
        app.setStartDate(new DateMidnight(2012, 1, 17));
        app.setEndDate(new DateMidnight(2012, 1, 12));
        instance.validate(app, errors);
        Mockito.verify(errors).reject("error.period");
        Mockito.reset(errors);

        // if application's year is later than maximum permissible date
        // is tested in PropertiesValidatorTest: test for method validateMaximumVacationProperty

        // test if everything is ok
        app.setStartDate(new DateMidnight(2012, 1, 17));
        app.setEndDate(new DateMidnight(2012, 1, 20));
        app.setVacationType(VacationType.SPECIALLEAVE);
        app.setReason("Hochzeit");
        instance.validate(app, errors);
        Mockito.verifyZeroInteractions(errors);

        // String length

        app.setAddress(
            "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. ");
        instance.validate(app, errors);
        Mockito.verify(errors).rejectValue("address", "error.length");
        Mockito.reset(errors);

        app.setAddress("normale Adresse");
        instance.validate(app, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        app.setReason(
            "Freilebende Gummibärchen gibt es nicht. Man kauft sie in Packungen an der Kinokasse. Dieser Kauf ist der Beginn einer fast erotischen und sehr ambivalenten Beziehung Gummibärchen-Mensch. Zuerst gen...toller Text ist das, einfach wow!");
        instance.validate(app, errors);
        Mockito.verify(errors).rejectValue("reason", "error.length");
        Mockito.reset(errors);

        app.setReason("normaler Grund");
        instance.validate(app, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        app.setComment("kleiner Kommentar");
        instance.validate(app, errors);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        app.setComment(
            "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis");
        instance.validate(app, errors);
        Mockito.verify(errors).rejectValue("comment", "error.length");
        Mockito.reset(errors);
    }


    /**
     * Test of validateComment method, of class ApplicationValidator.
     */
    @Test
    public void testValidateComment() {

        Comment comment = new Comment();

        comment.setReason(null);
        instance.validateComment(comment, errors, true);
        Mockito.verify(errors).rejectValue("reason", "error.reason");
        Mockito.reset(errors);

        comment.setReason("");
        instance.validateComment(comment, errors, true);
        Mockito.verify(errors).rejectValue("reason", "error.reason");
        Mockito.reset(errors);

        comment.setReason("Aus gutem Grund");
        instance.validateComment(comment, errors, true);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        comment.setReason(
            "Freilebende Gummibärchen gibt es nicht. Man kauft sie in Packungen an der Kinokasse. Dieser Kauf ist der Beginn einer fast erotischen und sehr ambivalenten Beziehung Gummibärchen-Mensch. Zuerst gen...toller Text ist das, einfach wow!");
        instance.validateComment(comment, errors, true);
        Mockito.verify(errors).rejectValue("reason", "error.length");
    }


    @Test
    public void testValidateCommentMayBeEmptyIfNotMandatory() {

        Comment comment = new Comment();

        comment.setReason(null);
        instance.validateComment(comment, errors, false);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        comment.setReason("");
        instance.validateComment(comment, errors, false);
        Mockito.verifyZeroInteractions(errors);
        Mockito.reset(errors);

        // has error if text is too long
        comment.setReason(
            "Freilebende Gummibärchen gibt es nicht. Man kauft sie in Packungen an der Kinokasse. Dieser Kauf ist der Beginn einer fast erotischen und sehr ambivalenten Beziehung Gummibärchen-Mensch. Zuerst gen...toller Text ist das, einfach wow!");
        instance.validateComment(comment, errors, false);
        Mockito.verify(errors).rejectValue("reason", "error.length");
    }


    /**
     * Test of validateStringLength method, of class ApplicationValidator.
     */
    @Test
    public void testValidateStringLength() {

        String text = "riesengroße begründung, die kein mensch braucht, so ein Quatsch!";
        boolean returnValue = instance.validateStringLength(text, 50);
        assertNotNull(returnValue);
        assertFalse(returnValue);

        text = "kurze knackige Begründung";
        returnValue = instance.validateStringLength(text, 50);
        assertNotNull(returnValue);
        assertTrue(returnValue);
    }


    @Test
    public void testValidateAppForTodayFullDay() {

        Model model = Mockito.mock(Model.class);

        DateMidnight today = DateMidnight.now();

        app.setHowLong(DayLength.FULL);
        app.setStartDate(today);

        instance.validatePast(app, errors, model);
        Mockito.verifyZeroInteractions(model);
    }


    @Test
    public void testValidateAppForYesterdayFullDay() {

        Model model = Mockito.mock(Model.class);

        DateMidnight yesterday = DateMidnight.now().minusDays(1);

        app.setHowLong(DayLength.FULL);
        app.setStartDate(yesterday);

        instance.validatePast(app, errors, model);
        Mockito.verify(model).addAttribute("timeError", "error.period.past");
        Mockito.verify(model).addAttribute("setForce", 1);
    }


    @Test
    public void testValidateAppForTodayMorning() {

        Model model = Mockito.mock(Model.class);

        DateMidnight today = DateMidnight.now();

        app.setHowLong(DayLength.MORNING);
        app.setStartDateHalf(today);

        instance.validatePast(app, errors, model);
        Mockito.verifyZeroInteractions(model);
    }


    @Test
    public void testValidateAppForTodayAfternoon() {

        Model model = Mockito.mock(Model.class);

        DateMidnight today = DateMidnight.now();

        app.setHowLong(DayLength.MORNING);
        app.setStartDateHalf(today);

        instance.validatePast(app, errors, model);
        Mockito.verifyZeroInteractions(model);
    }


    @Test
    public void testValidateAppForYesterdayMorning() {

        Model model = Mockito.mock(Model.class);

        DateMidnight yesterday = DateMidnight.now().minusDays(1);

        app.setHowLong(DayLength.MORNING);
        app.setStartDateHalf(yesterday);

        instance.validatePast(app, errors, model);
        Mockito.verify(model).addAttribute("timeError", "error.period.past");
        Mockito.verify(model).addAttribute("setForce", 1);
    }


    @Test
    public void testValidateAppForYesterdayAfternoon() {

        Model model = Mockito.mock(Model.class);

        DateMidnight yesterday = DateMidnight.now().minusDays(1);

        app.setHowLong(DayLength.MORNING);
        app.setStartDateHalf(yesterday);

        instance.validatePast(app, errors, model);
        Mockito.verify(model).addAttribute("timeError", "error.period.past");
        Mockito.verify(model).addAttribute("setForce", 1);
    }


    @Test
    public void testValidateAppForVeryPastDate() {

        Model model = Mockito.mock(Model.class);

        DateMidnight date = new DateMidnight(2011, DateTimeConstants.SEPTEMBER, 1);

        app.setHowLong(DayLength.FULL);
        app.setStartDate(date);

        instance.validatePast(app, errors, model);
        Mockito.verify(model).addAttribute("timeError", "error.period.past.wide");
        Mockito.verify(model).addAttribute("setForce", 0);
    }
}
