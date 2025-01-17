package org.synyx.urlaubsverwaltung.overtime;

import org.springframework.stereotype.Service;

@Service
public class OvertimeImportService {

    private final OvertimeRepository overtimeRepository;
    private final OvertimeCommentRepository overtimeCommentRepository;

    OvertimeImportService(OvertimeRepository overtimeRepository, OvertimeCommentRepository overtimeCommentRepository) {
        this.overtimeRepository = overtimeRepository;
        this.overtimeCommentRepository = overtimeCommentRepository;
    }

    public void deleteAll() {
        overtimeCommentRepository.deleteAll();
        overtimeRepository.deleteAll();
    }

    public Overtime importOvertime(Overtime overtime) {
        return overtimeRepository.save(overtime);
    }


    public void importOvertimeComment(OvertimeComment overtimeComment) {
        overtimeCommentRepository.save(overtimeComment);
    }

}
