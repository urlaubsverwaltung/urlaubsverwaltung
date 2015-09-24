package org.synyx.urlaubsverwaltung.web.application;

import lombok.Data;

/**
 * Represents a comment.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Data
public class CommentForm {

    private String text;
    private boolean isMandatory;
}
