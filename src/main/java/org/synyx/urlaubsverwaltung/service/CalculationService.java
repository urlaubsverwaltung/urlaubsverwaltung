/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.synyx.urlaubsverwaltung.domain.Application;

/**
 * This service calculates if a person may apply for leave, i.e. if he/she has enough vacation days to apply for leave.
 * 
 * @author Aljona Murygina - murygina@synyx.de
 */
public class CalculationService {
    
    /**
     * check if application is valid and may be send to boss to be allowed or rejected or if person's leave account has
     * too little residual number of vacation days, so that taking holiday isn't possible
     *
     * @param  application
     *
     * @return  boolean: true if application is okay, false if there are too little residual number of vacation days
     */
    boolean checkApplication(Application application) {
        
        return false;
        
    }
    
}
