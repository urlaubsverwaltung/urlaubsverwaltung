package org.synyx.urlaubsverwaltung.mail;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Observes sending mails as {@code mail.connect} and {@code mail.send}, enabled via {@code uv.mail.observation.enabled}.
 *
 * <p>Replaces the {@link JavaMailSenderImpl} configured by Spring Boot with an {@link ObservedJavaMailSender} copy
 * instead of defining an own {@code JavaMailSender} bean, so the {@code spring.mail.*} configuration of Spring Boot
 * stays in charge.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "uv.mail.observation.enabled", havingValue = "true")
class MailObservationConfiguration {

    @Bean
    static BeanPostProcessor observedJavaMailSenderBeanPostProcessor(ObjectProvider<ObservationRegistry> observationRegistry) {
        return new ObservedJavaMailSenderBeanPostProcessor(observationRegistry);
    }

    static final class ObservedJavaMailSenderBeanPostProcessor implements BeanPostProcessor {

        private final ObjectProvider<ObservationRegistry> observationRegistry;

        ObservedJavaMailSenderBeanPostProcessor(ObjectProvider<ObservationRegistry> observationRegistry) {
            this.observationRegistry = observationRegistry;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            // exact type match: subclasses (incl. ObservedJavaMailSender) may carry custom behaviour a copy would lose
            if (bean.getClass() != JavaMailSenderImpl.class) {
                return bean;
            }

            final ObservationRegistry registry = observationRegistry.getIfAvailable();
            if (registry == null) {
                return bean;
            }

            return ObservedJavaMailSender.copyOf((JavaMailSenderImpl) bean, registry);
        }
    }
}
