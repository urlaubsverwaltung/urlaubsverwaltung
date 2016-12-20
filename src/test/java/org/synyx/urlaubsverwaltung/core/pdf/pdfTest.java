package org.synyx.urlaubsverwaltung.core.pdf;
import org.junit.Test;


import java.io.IOException;

import static org.junit.Assert.assertNotNull;

/**
 * Created by jan on 12/16/16.
 */
public class pdfTest extends IOException {

    pdfMaker pdftest = new pdfMaker(25,500);

    @Test
    public void testpdf() throws IOException{


        pdftest.textEingabe("Hallo ich werde getestet \n Eine weitere Zeile",14);
        byte[] testarray = pdftest.pdfCreator();
        assertNotNull(testarray);
    }

}
