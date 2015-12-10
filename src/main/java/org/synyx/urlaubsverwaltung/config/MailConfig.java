package org.synyx.urlaubsverwaltung.config;

import org.apache.velocity.app.VelocityEngine;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;


/**
 * Configuration to send mails.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Configuration
public class MailConfig {

    @Bean(name = "mailSender")
    public JavaMailSenderImpl javaMailSender() {

        return new JavaMailSenderImpl();
    }


    @Bean(name = "velocityEngine")
    public VelocityEngine velocityEngine() {

        Properties velocityProperties = new Properties();

        velocityProperties.put("resource.loader", "class");
        velocityProperties.put("class.resource.loader.class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityProperties.put("file.resource.loader.cache", true);
        velocityProperties.put("file.resource.loader.unicode", true);
        velocityProperties.put("input.encoding", "UTF-8");
        velocityProperties.put("output.encoding", "UTF-8");

        return new VelocityEngine(velocityProperties);
    }
}
