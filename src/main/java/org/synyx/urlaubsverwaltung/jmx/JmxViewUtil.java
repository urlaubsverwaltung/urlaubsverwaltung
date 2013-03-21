
package org.synyx.urlaubsverwaltung.jmx;

import java.util.ArrayList;
import java.util.List;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.person.Person;

/**
 *
 * @author Aljona Murygina
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
                result.add(String.format("%8d", app.getId()) + String.format("%20s", app.getApplicationDate().toString("dd.MM.yyyy"))
                    + String.format("%18s", person.getFirstName() + " " + person.getLastName()));
            }
        }
        
        return result;
        
    }
    
}
