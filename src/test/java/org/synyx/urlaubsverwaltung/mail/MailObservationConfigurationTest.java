package org.synyx.urlaubsverwaltung.mail;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import jakarta.activation.FileTypeMap;
import jakarta.activation.MimetypesFileTypeMap;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.mail.autoconfigure.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MailObservationConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(MailSenderAutoConfiguration.class))
        .withUserConfiguration(MailObservationConfiguration.class)
        .withPropertyValues(
            "spring.mail.host=smtp.example.org",
            "spring.mail.port=587",
            "spring.mail.username=user",
            "spring.mail.password=secret",
            "spring.mail.default-encoding=UTF-8",
            "spring.mail.properties.mail.smtp.connectiontimeout=5000"
        );

    @Test
    void replacesMailSenderWithObservedCopyOfBootConfiguration() {
        contextRunner
            .withUserConfiguration(ObservationConfiguration.class)
            .withPropertyValues("uv.mail.observation.enabled=true")
            .run(context -> assertThat(context.getBean(JavaMailSender.class))
                .isInstanceOfSatisfying(ObservedJavaMailSender.class, mailSender -> {
                    assertThat(mailSender.getHost()).isEqualTo("smtp.example.org");
                    assertThat(mailSender.getPort()).isEqualTo(587);
                    assertThat(mailSender.getUsername()).isEqualTo("user");
                    assertThat(mailSender.getPassword()).isEqualTo("secret");
                    assertThat(mailSender.getDefaultEncoding()).isEqualTo("UTF-8");
                    assertThat(mailSender.getJavaMailProperties()).containsEntry("mail.smtp.connectiontimeout", "5000");
                    assertThat(mailSender.getSession().getProperty("mail.smtp.connectiontimeout")).isEqualTo("5000");
                }));
    }

    @Test
    void keepsMailSenderWithoutProperty() {
        contextRunner
            .withUserConfiguration(ObservationConfiguration.class)
            .run(context -> assertThat(context.getBean(JavaMailSender.class))
                .isExactlyInstanceOf(JavaMailSenderImpl.class));
    }

    @Test
    void keepsMailSenderWhenDisabled() {
        contextRunner
            .withUserConfiguration(ObservationConfiguration.class)
            .withPropertyValues("uv.mail.observation.enabled=false")
            .run(context -> assertThat(context.getBean(JavaMailSender.class))
                .isExactlyInstanceOf(JavaMailSenderImpl.class));
    }

    @Test
    void keepsMailSenderWithoutObservationRegistry() {
        contextRunner
            .withPropertyValues("uv.mail.observation.enabled=true")
            .run(context -> assertThat(context.getBean(JavaMailSender.class))
                .isExactlyInstanceOf(JavaMailSenderImpl.class));
    }

    @Test
    void keepsSubclassedMailSender() {
        contextRunner
            .withUserConfiguration(ObservationConfiguration.class, CustomMailSenderConfiguration.class)
            .withPropertyValues("uv.mail.observation.enabled=true")
            .run(context -> assertThat(context.getBean(JavaMailSender.class))
                .isInstanceOf(CustomMailSender.class)
                .isNotInstanceOf(ObservedJavaMailSender.class));
    }

    @Test
    void recordsFailedConnectAsMetric() {
        contextRunner
            .withUserConfiguration(ObservationConfiguration.class)
            .withPropertyValues(
                "uv.mail.observation.enabled=true",
                "spring.mail.host=localhost",
                "spring.mail.port=1",
                "spring.mail.properties.mail.smtp.connectiontimeout=1000"
            )
            .run(context -> {
                final JavaMailSender mailSender = context.getBean(JavaMailSender.class);
                final MimeMessage message = mailSender.createMimeMessage();

                assertThatThrownBy(() -> mailSender.send(message)).isInstanceOf(MailSendException.class);

                final MeterRegistry meterRegistry = context.getBean(MeterRegistry.class);
                final Timer connect = meterRegistry.find("mail.connect")
                    .tag("error", "MailConnectException")
                    .tag("server.address", "localhost")
                    .tag("server.port", "1")
                    .tag("network.protocol.name", "smtp")
                    .timer();
                assertThat(connect).isNotNull();
                assertThat(connect.count()).isEqualTo(1);
                assertThat(meterRegistry.find("mail.send").timer()).isNull();
            });
    }

    @Test
    void copyOfCopiesAllFieldsOfJavaMailSenderImpl() throws IllegalAccessException {
        final Properties javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.smtp.connectiontimeout", "5000");
        final FileTypeMap fileTypeMap = new MimetypesFileTypeMap();

        final JavaMailSenderImpl original = new JavaMailSenderImpl();
        original.setJavaMailProperties(javaMailProperties);
        original.setProtocol("smtps");
        original.setHost("smtp.example.org");
        original.setPort(465);
        original.setUsername("user");
        original.setPassword("secret");
        original.setDefaultEncoding("UTF-8");
        original.setDefaultFileTypeMap(fileTypeMap);

        final ObservedJavaMailSender copy = ObservedJavaMailSender.copyOf(original, ObservationRegistry.create());

        assertThat(copy.getSession()).isSameAs(original.getSession());
        for (Field field : JavaMailSenderImpl.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !field.getName().equals("session")) {
                field.setAccessible(true);
                assertThat(field.get(copy)).as(field.getName()).isEqualTo(field.get(original));
            }
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class ObservationConfiguration {

        @Bean
        SimpleMeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }

        @Bean
        ObservationRegistry observationRegistry(MeterRegistry meterRegistry) {
            final ObservationRegistry observationRegistry = ObservationRegistry.create();
            observationRegistry.observationConfig().observationHandler(DefaultMeterObservationHandler.builder(meterRegistry).build());
            return observationRegistry;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomMailSenderConfiguration {

        @Bean
        JavaMailSender customMailSender() {
            return new CustomMailSender();
        }
    }

    static class CustomMailSender extends JavaMailSenderImpl {
    }
}
