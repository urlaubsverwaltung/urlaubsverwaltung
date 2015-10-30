package org.synyx.urlaubsverwaltung.core.person;

import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.SpringApplicationConfiguration;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import org.synyx.urlaubsverwaltung.UrlaubsverwaltungApplication;

import java.util.List;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = UrlaubsverwaltungApplication.class)
@WebAppConfiguration
public class PersonServiceIT {

    @Autowired
    private PersonService personService;

    @Test
    public void test() {

        List<Person> activePersons = personService.getActivePersons();
    }
}
