package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.stereotype.Service;

@Service
public class ApplicationImportService {

    private final ApplicationRepository applicationRepository;

    ApplicationImportService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public void deleteAll() {
        applicationRepository.deleteAll();
    }

    public ApplicationEntity importApplication(ApplicationEntity applicationEntity) {
        return applicationRepository.save(applicationEntity);
    }
}
