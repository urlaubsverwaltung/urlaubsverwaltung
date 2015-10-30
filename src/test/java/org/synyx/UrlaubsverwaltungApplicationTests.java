package org.synyx;

import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.boot.test.SpringApplicationConfiguration;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import org.synyx.urlaubsverwaltung.UrlaubsverwaltungApplication;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = UrlaubsverwaltungApplication.class)
@WebAppConfiguration
public class UrlaubsverwaltungApplicationTests {

    @Test
    public void contextLoads() {
    }
}
