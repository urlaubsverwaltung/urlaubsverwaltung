package org.synyx.urlaubsverwaltung.mail.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WebConfiguredMailConfigIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void hasWebConfiguredMailconfig() {
        assertThat(applicationContext.containsBean("webConfiguredMailConfig")).isTrue();
        assertThat(applicationContext.containsBean("springBootConfiguredMailConfig")).isFalse();
    }
}
