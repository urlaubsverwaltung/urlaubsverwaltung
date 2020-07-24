package org.synyx.urlaubsverwaltung.mail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.synyx.urlaubsverwaltung.TestContainersBase;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
    "spring.mail.host=my.smtp.server",
    "spring.mail.port=1025",
    "uv.mail.sender=sender@example.org",
    "uv.mail.administrator=admin@example.org",
    "uv.mail.application-url=http://localhost:8080"
})
class MailConfigurationIT extends TestContainersBase {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void hasSpringBootConfiguredMailConfig() {
        assertThat(applicationContext.containsBean("mailConfiguration")).isTrue();
        assertThat(applicationContext.containsBean("webConfiguredMailConfig")).isFalse();
    }
}
