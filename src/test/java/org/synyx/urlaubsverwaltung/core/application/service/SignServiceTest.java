package org.synyx.urlaubsverwaltung.core.application.service;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.util.CryptoUtil;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.security.NoSuchAlgorithmException;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SignServiceTest {

    private SignService signService;

    @Before
    public void setUp() {

        signService = new SignService(Mockito.mock(MailService.class));
    }


    @Test
    public void ensureSigningApplicationByUserGeneratesPersonSignature() throws NoSuchAlgorithmException {

        VacationType vacationType = Mockito.mock(VacationType.class);

        Person person = TestDataCreator.createPerson();
        Application application = TestDataCreator.createApplication(person, vacationType);

        // person needs some info: private key, last name
        person.setPrivateKey(CryptoUtil.generateKeyPair().getPrivate().getEncoded());

        // application needs data
        application.setPerson(person);
        application.setVacationType(vacationType);
        application.setApplicationDate(new DateMidnight(2011, 11, 1));

        signService.signApplicationByUser(application, person);

        // signature of person should be filled, signature of boss not
        Assert.assertNotNull(application.getSignaturePerson());
        Assert.assertNull(application.getSignatureBoss());
    }


    @Test
    public void ensureSigningApplicationByBossGeneratesBossSignature() throws NoSuchAlgorithmException {

        VacationType vacationType = Mockito.mock(VacationType.class);
        Person person = TestDataCreator.createPerson();
        Application application = TestDataCreator.createApplication(person, vacationType);

        // person needs some info: private key, last name
        person.setPrivateKey(CryptoUtil.generateKeyPair().getPrivate().getEncoded());

        // application needs data
        application.setPerson(person);
        application.setVacationType(vacationType);
        application.setApplicationDate(new DateMidnight(2011, 12, 21));

        signService.signApplicationByBoss(application, person);

        // signature of boss should be filled, signature of person not
        Assert.assertNotNull(application.getSignatureBoss());
        Assert.assertNull(application.getSignaturePerson());
    }
}
