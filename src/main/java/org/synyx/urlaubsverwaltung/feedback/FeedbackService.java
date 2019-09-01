package org.synyx.urlaubsverwaltung.feedback;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class FeedbackService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private PersonService personService;

    public FeedbackService(PersonService personService) {
        this.personService = personService;
    }

    public void handleFeedback(Feedback feedback) {
        Person signedInUser = personService.getSignedInUser();
        LOG.info(String.format("got feedback=%s from user=%s", feedback, signedInUser));
    }
}
