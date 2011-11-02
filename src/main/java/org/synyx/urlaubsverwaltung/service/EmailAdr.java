/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

/**
 * @author  aljona
 */
public enum EmailAdr {

    CHEFS("kraft@synyx.de, arrasz@synyx.de, daniel@synyx.de"),
    OFFICE("office@synyx.de"),
    STERN("stern@synyx.de"),
    MANAGE("urlaubsverwaltung@synyx.de");

    private String email;

    private EmailAdr(String email) {

        this.email = email;
    }

    public String getEmail() {

        return this.email;
    }
}
