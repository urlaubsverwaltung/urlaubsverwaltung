package org.synyx.urlaubsverwaltung.core.pdf;

/**
 * Created by jan on 12/16/16.
 */
import org.junit.Test;


import javax.activation.DataSource;
import javax.mail.MessagingException;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class PdfDatasourceMakerTest {
    @Test
    public void DataSourceTest() throws IOException, MessagingException

    {
        pdfMaker pdftest = new pdfMaker(25, 250);
        pdftest.textEingabe("Ich werde getestet",14);
        byte[] testByteArray = pdftest.pdfCreator();

        PdfDatasourceMaker datasourceTest = new PdfDatasourceMaker();

        DataSource testsource = datasourceTest.mimeMaker(testByteArray);

    }
}
