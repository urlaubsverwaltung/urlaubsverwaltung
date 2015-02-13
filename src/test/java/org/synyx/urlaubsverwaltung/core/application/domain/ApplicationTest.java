package org.synyx.urlaubsverwaltung.core.application.domain;

import org.junit.Assert;
import org.junit.Test;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.core.application.domain.Application}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ApplicationTest {

    @Test
    public void ensureReturnsTrueIfItHasTheGivenStatus() {

        Application application = new Application();

        application.setStatus(ApplicationStatus.ALLOWED);

        Assert.assertTrue("Should return true if it has the given status",
            application.hasStatus(ApplicationStatus.ALLOWED));
    }


    @Test
    public void ensureReturnsFalseIfItHasNotTheGivenStatus() {

        Application application = new Application();

        application.setStatus(ApplicationStatus.CANCELLED);

        Assert.assertFalse("Should return false if it has the given status",
            application.hasStatus(ApplicationStatus.ALLOWED));
    }
}
