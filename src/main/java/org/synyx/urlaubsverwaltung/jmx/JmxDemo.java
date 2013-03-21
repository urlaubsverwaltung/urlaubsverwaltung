package org.synyx.urlaubsverwaltung.jmx;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.jmx.export.notification.NotificationPublisherAware;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.mail.MailService;

import java.util.List;

import javax.management.Notification;


/**
 * Ready jmx bean including annotations and notifying function. Manage waiting applications for leave with this class.
 * Send an email to boss for a certain application or for all waiting applications.
 *
 * @author  Aljona Murygina
 */
//@ManagedResource(objectName = "mbeans:name=myJmxDemoBean", description = "Manage some 'Urlaubsverwaltung' problems.")
public class JmxDemo implements NotificationPublisherAware {

    private long numberOfWaitingApplications;

    private NotificationPublisher notificationPublisher;

    private JmxApplicationService applicationService;
    private MailService mailService;

    public JmxDemo(JmxApplicationService applicationService, MailService mailService) {

        this.applicationService = applicationService;
        this.mailService = mailService;
    }

    @ManagedAttribute(description = "Get the number of all waiting applications")
    public long getNumberOfWaitingApplications() {

        this.numberOfWaitingApplications = applicationService.countWaitingApplications();

        return numberOfWaitingApplications;
    }


    @ManagedOperation(description = "Get the number of all applications that have the given status.")
    @ManagedOperationParameters(
        {
            @ManagedOperationParameter(
                name = "status", description = "The status may be waiting, allowed, rejected or cancelled."
            )
        }
    )
    public long countApplicationsInStatus(String state) throws IllegalArgumentException {

        state = state.toUpperCase();

        ApplicationStatus status = ApplicationStatus.valueOf(state);

        return applicationService.countApplicationsInStatus(status);
    }


    @ManagedOperation(description = "Shows a list of all waiting applications with some information.")
    public List<String> showWaitingApplications() {

        List<Application> applications = applicationService.getWaitingApplications();

        return JmxViewUtil.generateReturnList(applications);
    }

// use this maybe later, but not now for demo


//    @ManagedOperation(description = "Reminds the boss via email to decide about the current waiting applications.")
//    public String remindBossAboutWaitingApplications() {
//
//        List<Application> applications = applicationService.getWaitingApplications();
//
//        mailService.sendRemindingBossAboutWaitingApplicationsNotification(applications);
//
//        return "The boss got reminded via email now.";
//    }

    /**
     * is not annotated with managedoperation because you should not see this method, but it is just called by another
     * class if an error occurs
     */
    public void notifyAboutLogin(String msg) {

        notificationPublisher.sendNotification(new Notification("Login", this, 0, msg));
    }


    @Override
    public void setNotificationPublisher(NotificationPublisher notificationPublisher) {

        this.notificationPublisher = notificationPublisher;
    }
}
