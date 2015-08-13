
package org.synyx.urlaubsverwaltung.web.person;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.web.util.GravatarUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Constants concerning {@link PersonController}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public final class PersonConstants {

    // JPSs
    public static final String STAFF_JSP = "person/staff_view";
    public static final String PERSON_FORM_JSP = "person/person_form";

    // Attributes
    public static final String PERSONS_ATTRIBUTE = "persons";
    public static final String PERSON_ATTRIBUTE = "person";
    public static final String BEFORE_APRIL_ATTRIBUTE = "beforeApril";
    public static final String GRAVATAR_URLS_ATTRIBUTE = "gravatarUrls";

    private PersonConstants() {

        // Hide constructor for util classes
    }

    /**
     * Get a map of Gravatar URLs for the given persons.
     *
     * @param  persons  to get the Gravatar URLs for
     *
     * @return  mapping from persons to Gravatar URLs
     */
    public static Map<Person, String> getGravatarURLs(List<Person> persons) {

        Map<Person, String> gravatarUrls = new HashMap<>();

        for (Person person : persons) {
            String url = GravatarUtil.createImgURL(person.getEmail());

            if (url != null) {
                gravatarUrls.put(person, url);
            }
        }

        return gravatarUrls;
    }
}
