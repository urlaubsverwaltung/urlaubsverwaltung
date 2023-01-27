package de.focusshift.launchpad.core;

import de.focusshift.launchpad.api.LaunchpadAppUrlCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.net.URL;
import java.util.List;

@Configuration
@EnableConfigurationProperties(LaunchpadConfigProperties.class)
@ConditionalOnProperty(prefix="launchpad", name = "name-default-locale")
public class LaunchpadAutoConfiguration {

    @Conditional(LaunchpadAppsCondition.class)
    static class LaunchpadConfig {

        @Bean
        LaunchpadControllerAdvice launchpadControllerAdvice(LaunchpadService launchpadService) {
            return new LaunchpadControllerAdvice(launchpadService);
        }

        @Bean
        LaunchpadService launchpadService(LaunchpadConfigProperties launchpadConfigProperties, LaunchpadAppUrlCustomizer appUrlCustomizer) {
            return new LaunchpadServiceImpl(launchpadConfigProperties, appUrlCustomizer);
        }

        @Bean
        @ConditionalOnMissingBean
        LaunchpadAppUrlCustomizer appUrlCustomizer() {
            return URL::new;
        }
    }


    /**
     * Checks if 'launchpad.apps' are configured appropriate.
     */
    // copied and adjusted from org.springframework.boot.autoconfigure.condition.OnPropertyListCondition
    static class LaunchpadAppsCondition extends SpringBootCondition {
        private static final Bindable<List<LaunchpadConfigProperties.App>> APP_LIST = Bindable.listOf(LaunchpadConfigProperties.App.class);

        private static final String propertyName = "launchpad.apps";

        @Override
        public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
            final BindResult<?> property = Binder.get(context.getEnvironment()).bind(propertyName, APP_LIST);
            final ConditionMessage.Builder messageBuilder = ConditionMessage.forCondition("apps");
            if (property.isBound()) {
                return ConditionOutcome.match(messageBuilder.found("property").items(propertyName));
            }
            return ConditionOutcome.noMatch(messageBuilder.didNotFind("property").items(propertyName));
        }
    }
}
