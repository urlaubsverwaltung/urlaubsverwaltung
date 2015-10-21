package org.synyx.urlaubsverwaltung.dev;

import org.synyx.urlaubsverwaltung.core.startup.Environment;
import org.synyx.urlaubsverwaltung.core.util.SystemPropertyCondition;


/**
 * Condition matching if the application is running in development mode.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class DevEnvironmentCondition extends SystemPropertyCondition {

    public DevEnvironmentCondition() {

        super(Environment.PROPERTY_KEY, Environment.Type.DEV.getName());
    }
}
