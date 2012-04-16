package org.synyx.urlaubsverwaltung.jmx;

import java.util.ArrayList;
import java.util.List;
import javax.management.Notification;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.jmx.export.notification.NotificationPublisherAware;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.service.ApplicationService;
import org.synyx.urlaubsverwaltung.service.MailService;

/**
 * Manage waiting applications for leave with this class. Send an email to boss for a certain application or for all waiting applications.
 * 
 * @author Aljona Murygina
 */
@ManagedResource(objectName = "mbeans:name=myJmxDemoBean", description = "Manage some 'Urlaubsverwaltung' problems.")
public class JmxDemo implements NotificationPublisherAware {
    
    private long numberOfWaitingApplications;
    
    private NotificationPublisher notificationPublisher;
    
    private ApplicationService applicationService;
    private MailService mailService;

    public JmxDemo(ApplicationService applicationService, MailService mailService) {
        this.applicationService = applicationService;
        this.mailService = mailService;
    }

    @ManagedAttribute(description = "Get the number of all waiting applications" )
    public long getNumberOfWaitingApplications() {
        this.numberOfWaitingApplications = applicationService.countWaitingApplications();
        return numberOfWaitingApplications;
    }

    @ManagedOperation(description = "Get the number of all applications that have the given status.")
    @ManagedOperationParameters({
        @ManagedOperationParameter(name = "status", description = "The status may be waiting, allowed, rejected or cancelled.")
    })
    public long countApplicationsInStatus(String state) throws IllegalArgumentException {
        
        state = state.toUpperCase();
        
        ApplicationStatus status = ApplicationStatus.valueOf(state);
        return applicationService.countApplicationsInStatus(status);
        
    }

    @ManagedOperation(description = "Shows a list of all waiting applications with some information.")
    public List<String> showWaitingApplications() {
        
        List<Application> applications = applicationService.getWaitingApplications();
        
        return generateOutputListByListOfApplications(applications);
    }
    
    @ManagedOperation(description = "Reminds the boss via email to decide about the current waiting applications.")
    public String remindBossAboutWaitingApplications() {
        
        List<Application> applications = applicationService.getWaitingApplications();
        
        mailService.sendRemindingBossAboutWaitingApplicationsNotification(applications);
        
        return "The boss got reminded via email now.";
    }
    
    /**
     * is not annotated with managedoperation because you should not see this method, but it is just called if an error occurs
     */
    public void notifyAboutFailedKeyGeneration(String msg) {
        notificationPublisher.sendNotification(new Notification("Sign Error", this, 0, msg));
    }
    
    private List<String> generateOutputListByListOfApplications(List<Application> applications) {
        
        List<String> result = new ArrayList<String>();
        
        if (applications.isEmpty()) {
            result.add("There are no applications with status waiting.");
        } else {
            result.add(String.format("%8s", "ID") + String.format("%20s", "Application date")
                + String.format("%18s", "Person"));

            for (Application app : applications) {
                Person person = app.getPerson();
                result.add(String.format("%8d", app.getId()) + String.format("%20s", app.getApplicationDate().toString("dd.MM.yyyy"))
                    + String.format("%18s", person.getFirstName() + " " + person.getLastName()));
            }
        }
        
        return result;
        
    }

    @Override
    public void setNotificationPublisher(NotificationPublisher notificationPublisher) {
        this.notificationPublisher = notificationPublisher;
    }
    
}
