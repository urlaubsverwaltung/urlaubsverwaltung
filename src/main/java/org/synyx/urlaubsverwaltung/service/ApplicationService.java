
package org.synyx.urlaubsverwaltung.service;

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.Person;

/**
 * This service provides access to the applications for leave (domain object {@link Application}), i.e. saves or allows it, etc.
 * 
 * @author Aljona Murygina - murygina@synyx.de
 */
public interface ApplicationService {

    /**
     * get Id of latest Application with given Person and ApplicationStatus
     * 
     * @param person
     * @param status
     * @return int id (primary key of Application)
     */
    int getIdOfLatestApplication(Person person, ApplicationStatus status);

    /**
     * use this to get an application by its id
     *
     * @param  id
     *
     * @return
     */
    Application getApplicationById(Integer id);

    /**
     * use this to save a new application, state is set to waiting
     *
     * @param  application  the application to be saved
     */
    void save(Application application);

    /**
     * use this to set a application to allowed (only boss)
     *
     * @param  application  the application to be edited
     */
    void allow(Application application, Person boss);

    /**
     * use this to set a application to rejected (only boss)
     *
     * @param  application  the application to be edited
     */
    void reject(Application application, Person boss);

    /**
     * application's state is set to cancelled if user cancels vacation
     *
     * @param  application
     */
    void cancel(Application application);

    /**
     * signs an application with the private key of the signing user (applicant)
     *
     * @param  application
     * @param  user
     */
    void signApplicationByUser(Application application, Person user);

    /**
     * signs an application with the private key of the signing boss
     *
     * @param  application
     * @param  boss
     */
    void signApplicationByBoss(Application application, Person boss);
}
