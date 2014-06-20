
package org.synyx.urlaubsverwaltung.jmx;

import org.synyx.urlaubsverwaltung.DateFormat;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.ArrayList;
import java.util.List;


/**
 * Util class for JMX relevant purpose.
 *
 * @author  Aljona Murygina
 */
public class JmxViewUtil {

    public static List<String> generateReturnList(List<Application> applications) {

        List<String> result = new ArrayList<String>();

        if (applications.isEmpty()) {
            result.add("There are no applications with status waiting.");
        } else {
            result.add(String.format("%8s", "ID") + String.format("%20s", "Application date")
                + String.format("%18s", "Person"));

            for (Application app : applications) {
                Person person = app.getPerson();
                result.add(String.format("%8d", app.getId())
                    + String.format("%20s", app.getApplicationDate().toString(DateFormat.PATTERN))
                    + String.format("%18s", person.getFirstName() + " " + person.getLastName()));
            }
        }

        return result;
    }
}
