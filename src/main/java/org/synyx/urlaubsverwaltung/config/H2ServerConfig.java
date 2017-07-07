package org.synyx.urlaubsverwaltung.config;

import org.h2.tools.Server;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;


/**
 * Enables H2 Web Console.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Configuration
@ConditionalOnProperty("spring.h2.console.enabled")
public class H2ServerConfig {

    @Value("${h2.db.tcpPort}")
    private String h2TcpPort;

    @Value("${h2.db.webPort}")
    private String h2WebPort;

    @Bean
    public Server h2TcpServer() throws SQLException {

        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", h2TcpPort).start();
    }


    @Bean
    public Server h2WebServer() throws SQLException {

        return Server.createWebServer("-web", "-webAllowOthers", "-webPort", h2WebPort).start();
    }
}
