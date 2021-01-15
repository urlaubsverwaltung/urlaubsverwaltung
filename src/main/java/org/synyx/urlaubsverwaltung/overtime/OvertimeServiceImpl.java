package org.synyx.urlaubsverwaltung.overtime;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.util.DateUtil;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.math.BigDecimal.ZERO;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeAction.CREATED;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeAction.EDITED;


/**
 * @since 2.11.0
 */
@Transactional
@Service
class OvertimeServiceImpl implements OvertimeService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final OvertimeRepository overtimeRepository;
    private final OvertimeCommentRepository overtimeCommentRepository;
    private final ApplicationService applicationService;
    private final OvertimeMailService overtimeMailService;
    private final Clock clock;

    @Autowired
    public OvertimeServiceImpl(OvertimeRepository overtimeRepository, OvertimeCommentRepository overtimeCommentRepository,
                               ApplicationService applicationService, OvertimeMailService overtimeMailService,
                               Clock clock) {

        this.overtimeRepository = overtimeRepository;
        this.overtimeCommentRepository = overtimeCommentRepository;
        this.applicationService = applicationService;
        this.overtimeMailService = overtimeMailService;
        this.clock = clock;
    }

    @Override
    public List<Overtime> getOvertimeRecordsForPerson(Person person) {

        Assert.notNull(person, "Person to get overtime records for must be given.");

        return overtimeRepository.findByPerson(person);
    }

    @Override
    public List<Overtime> getOvertimeRecordsForPersonAndYear(Person person, int year) {

        Assert.notNull(person, "Person to get overtime records for must be given.");

        return overtimeRepository.findByPersonAndPeriod(person, DateUtil.getFirstDayOfYear(year), DateUtil.getLastDayOfYear(year));
    }

    @Override
    public Overtime record(Overtime overtime, Optional<String> comment, Person author) {

        final boolean isNewOvertime = overtime.isNew();

        // save overtime record
        overtime.onUpdate();
        overtimeRepository.save(overtime);

        // save comment
        final OvertimeAction action = isNewOvertime ? CREATED : EDITED;
        OvertimeComment overtimeComment = new OvertimeComment(author, overtime, action, clock);
        comment.ifPresent(overtimeComment::setText);

        overtimeCommentRepository.save(overtimeComment);

        overtimeMailService.sendOvertimeNotification(overtime, overtimeComment);

        LOG.info("{} overtime record: {}", isNewOvertime ? "Created" : "Updated", overtime);

        return overtime;
    }

    @Override
    public Optional<Overtime> getOvertimeById(Integer id) {

        Assert.notNull(id, "ID must be given.");

        return overtimeRepository.findById(id);
    }

    @Override
    public List<OvertimeComment> getCommentsForOvertime(Overtime overtime) {

        Assert.notNull(overtime, "Overtime record to get comments for must be given.");

        return overtimeCommentRepository.findByOvertime(overtime);
    }

    @Override
    public BigDecimal getTotalOvertimeForPersonAndYear(Person person, int year) {

        Assert.notNull(person, "Person to get total overtime for must be given.");
        Assert.isTrue(year > 0, "Year must be a valid number.");

        final List<Overtime> overtimeRecords = getOvertimeRecordsForPersonAndYear(person, year);

        BigDecimal totalHours = ZERO;
        for (Overtime record : overtimeRecords) {
            totalHours = totalHours.add(record.getHours());
        }

        return totalHours;
    }

    @Override
    public BigDecimal getLeftOvertimeForPerson(Person person) {

        Assert.notNull(person, "Person to get left overtime for must be given.");

        final BigDecimal totalOvertime = getTotalOvertimeForPerson(person);
        final BigDecimal overtimeReduction = applicationService.getTotalOvertimeReductionOfPerson(person);

        return totalOvertime.subtract(overtimeReduction);
    }

    private BigDecimal getTotalOvertimeForPerson(Person person) {

        final Optional<BigDecimal> totalOvertime = Optional.ofNullable(overtimeRepository.calculateTotalHoursForPerson(person));
        return totalOvertime.orElse(ZERO);

    }
}
