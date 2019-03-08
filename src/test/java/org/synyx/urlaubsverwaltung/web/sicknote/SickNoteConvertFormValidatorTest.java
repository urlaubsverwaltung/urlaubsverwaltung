package org.synyx.urlaubsverwaltung.web.sicknote;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;


/**
 * Unit test for {@link SickNoteConvertFormValidator}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SickNoteConvertFormValidatorTest {

    private SickNoteConvertFormValidator validator;

    private Errors errors;

    @Before
    public void setUp() {

        validator = new SickNoteConvertFormValidator();
        errors = Mockito.mock(Errors.class);
    }


    @Test
    public void ensureNullReasonIsNotValid() {

        SickNoteConvertForm convertForm = new SickNoteConvertForm(new SickNote());

        convertForm.setReason(null);

        validator.validate(convertForm, errors);

        Mockito.verify(errors).rejectValue("reason", "error.entry.mandatory");
    }


    @Test
    public void ensureEmptyReasonIsNotValid() {

        SickNoteConvertForm convertForm = new SickNoteConvertForm(new SickNote());

        convertForm.setReason("");

        validator.validate(convertForm, errors);

        Mockito.verify(errors).rejectValue("reason", "error.entry.mandatory");
    }


    @Test
    public void ensureThereIsAMaximumCharLengthForReason() {

        SickNoteConvertForm convertForm = new SickNoteConvertForm(new SickNote());

        convertForm.setReason(
            "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt"
            + " ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud "
            + "exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. ");

        validator.validate(convertForm, errors);

        Mockito.verify(errors).rejectValue("reason", "error.entry.tooManyChars");
    }
}
