package org.synyx.urlaubsverwaltung.core.comment;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class AbstractCommentTest {

    @Test
    public void ensureHasDateSetAfterInitialization() {

        TestComment comment = new TestComment();

        Assert.assertNotNull("Should not be null", comment.getDate());
        Assert.assertEquals("Wrong date", DateMidnight.now(), comment.getDate());
    }

    private class TestComment extends AbstractComment {

        private TestComment() {

            super();
        }
    }
}
