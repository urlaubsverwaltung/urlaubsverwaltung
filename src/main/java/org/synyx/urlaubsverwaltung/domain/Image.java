/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.domain;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.Lob;


/**
 * @author  aljona
 */

@Embeddable
public class Image implements Serializable {

    private static final long serialVersionUID = 1L;

    @Lob
    private byte[] binaerdaten;

    private String name;

    public Image() {
    }


    public Image(byte[] binaerdaten, String name) {

        this.binaerdaten = binaerdaten;
        this.name = name;
    }

    public byte[] getBinaerdaten() {

        return binaerdaten;
    }


    public void setBinaerdaten(byte[] binaerdaten) {

        this.binaerdaten = binaerdaten;
    }


    public String getName() {

        return name;
    }


    public void setName(String name) {

        this.name = name;
    }
}
