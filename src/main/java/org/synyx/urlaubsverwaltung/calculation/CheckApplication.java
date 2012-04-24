/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.calculation;

import org.synyx.urlaubsverwaltung.domain.Application;

/**
 *
 * @author Aljona Murygina
 */
public class CheckApplication {
    
    private CalculationMethodsHandler handler;

    public CheckApplication(CalculationMethodsHandler handler) {
        this.handler = handler;
    }
    
    boolean checkApp(Application application) {
        
        // days, daysBeforeApril, daysAfterApril of application are set here
        handler.forwardToCalculationStrategy(application);
        
        // now check if the user has enough vacation days to apply for leave
        
        
        return true;
    }
    
}
