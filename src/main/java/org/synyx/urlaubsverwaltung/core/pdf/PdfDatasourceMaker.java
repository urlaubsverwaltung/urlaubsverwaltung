package org.synyx.urlaubsverwaltung.core.pdf;

/**
 * Created by janrosum on 12/16/16.
 */

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.util.ByteArrayDataSource;

public class PdfDatasourceMaker {

    public DataSource mimeMaker(byte[] bytes) throws MessagingException {

    DataSource dataSource = new ByteArrayDataSource(bytes, "application/pdf");

    return dataSource;
    }
}
