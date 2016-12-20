package org.synyx.urlaubsverwaltung.core.pdf;

/**
 * Created by janrosum on 12/16/16.
 */
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;


import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class pdfMaker {

    private final PDDocument document = new PDDocument();
    private final PDPage my_page = new PDPage();
    private final int tx;
    private final int ty;

    public pdfMaker(int tx, int ty) {
        this.tx = tx;
        this.ty = ty;
    }


    public  void textEingabe(String text, int fontsize){

        try {
            PDPageContentStream texteingabe = new PDPageContentStream(this.document, this.my_page);

            texteingabe.beginText();
            texteingabe.setFont(PDType1Font.TIMES_ROMAN,fontsize);
            texteingabe.setLeading(15f);
            texteingabe.newLineAtOffset(this.tx, this.ty);


            String[] textarray = text.split("\n");
            int i = 0;
            int ii = textarray.length;
            while (i < ii) {

                texteingabe.showText(textarray[i]);
                texteingabe.newLine();


                i++;
            }
            texteingabe.endText();
            texteingabe.close();
        } catch (IOException e) {
            try{
                e.printStackTrace();
            }

            finally {
                try {
                    this.document.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        }


    }

    public byte[] pdfCreator() throws IOException {
        ByteArrayOutputStream pdfstream = new ByteArrayOutputStream();
        this.document.addPage(this.my_page);
        this.document.save(pdfstream);
        byte[] pdfarray = pdfstream.toByteArray();
        pdfstream.close();
        this.document.close();
        return pdfarray;

    }


}