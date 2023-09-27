package org.synyx.urlaubsverwaltung.ui.extension;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith({ BrowserSetupExtension.class, PageParameterResolver.class })
@ContextConfiguration(initializers = UITestInitializer.class)
public @interface UiTest {
}
