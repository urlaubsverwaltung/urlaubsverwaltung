package org.synyx.urlaubsverwaltung.workingtime;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkingTimeImportService {

    private final WorkingTimeRepository workingTimeRepository;

    WorkingTimeImportService(WorkingTimeRepository workingTimeRepository) {
        this.workingTimeRepository = workingTimeRepository;
    }

    public void deleteAll() {
        workingTimeRepository.deleteAll();
    }

    public void importWorkingTimes(List<WorkingTimeEntity> workingTimeEntities) {
        workingTimeRepository.saveAll(workingTimeEntities);
    }
}
