package org.synyx.urlaubsverwaltung.restapi;

/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
class BaseResponse {

    private String version;
    private String userName;

    BaseResponse(String version, String userName) {

        this.version = version;
        this.userName = userName;
    }

    public String getVersion() {

        return version;
    }


    public void setVersion(String version) {

        this.version = version;
    }


    public String getUserName() {

        return userName;
    }


    public void setUserName(String userName) {

        this.userName = userName;
    }
}
