package org.synyx.urlaubsverwaltung.sicknote.web;

/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class SearchRequest {

    private Integer personId;

    private String from;

    private String to;

    public Integer getPersonId() {

        return personId;
    }


    public void setPersonId(Integer personId) {

        this.personId = personId;
    }


    public String getFrom() {

        return from;
    }


    public void setFrom(String from) {

        this.from = from;
    }


    public String getTo() {

        return to;
    }


    public void setTo(String to) {

        this.to = to;
    }
}
