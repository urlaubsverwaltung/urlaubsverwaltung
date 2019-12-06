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
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;


/**
 * @since 2.11.0
 */
@Transactional
@Service
class OvertimeServiceImpl implements OvertimeService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final OvertimeDAO overtimeDAO;
    private final OvertimeCommentDAO commentDAO;
    private final ApplicationService applicationService;
    private final OvertimeMailService overtimeMailService;

    @Autowired
    public OvertimeServiceImpl(OvertimeDAO overtimeDAO, OvertimeCommentDAO commentDAO,
                               ApplicationService applicationService, OvertimeMailService overtimeMailService) {

        this.overtimeDAO = overtimeDAO;
        this.commentDAO = commentDAO;
        this.applicationService = applicationService;
        this.overtimeMailService = overtimeMailService;
    }

    @Override
    public List<Overtime> getOvertimeRecordsForPerson(Person person) {

        Assert.notNull(person, "Person to get overtime records for must be given.");

        return overtimeDAO.findByPerson(person);
    }


    @Override
    public List<Overtime> getOvertimeRecordsForPersonAndYear(Person person, int year) {

        Assert.notNull(person, "Person to get overtime records for must be given.");

        return overtimeDAO.findByPersonAndPeriod(person, DateUtil.getFirstDayOfYear(year), DateUtil.getLastDayOfYear(year));
    }


    @Override
    public Overtime record(Overtime overtime, Optional<String> comment, Person author) {

        boolean isNewOvertime = overtime.isNew();

        // save overtime record
        overtime.onUpdate();
        overtimeDAO.save(overtime);

        // save comment
        OvertimeAction action = isNewOvertime ? OvertimeAction.CREATED : OvertimeAction.EDITED;
        OvertimeComment overtimeComment = new OvertimeComment(author, overtime, action);
        comment.ifPresent(overtimeComment::setText);

        commentDAO.save(overtimeComment);

        overtimeMailService.sendOvertimeNotification(overtime, overtimeComment);

        LOG.info("{} overtime record: {}", isNewOvertime ? "Created" : "Updated", overtime);

        return overtime;
    }


    @Override
    public Optional<Overtime> getOvertimeById(Integer id) {

        Assert.notNull(id, "ID must be given.");

        return overtimeDAO.findById(id);
    }


    @Override
    public List<OvertimeComment> getCommentsForOvertime(Overtime overtime) {

        Assert.notNull(overtime, "Overtime record to get comments for must be given.");

        return commentDAO.findByOvertime(overtime);
    }


    @Override
    public BigDecimal getTotalOvertimeForPersonAndYear(Person person, int year) {

        Assert.notNull(person, "Person to get total overtime for must be given.");
        Assert.isTrue(year > 0, "Year must be a valid number.");

        List<Overtime> overtimeRecords = getOvertimeRecordsForPersonAndYear(person, year);

        BigDecimal totalHours = BigDecimal.ZERO;

        for (Overtime record : overtimeRecords) {
            totalHours = totalHours.add(record.getHours());
        }

        return totalHours;
    }


    @Override
    public BigDecimal getLeftOvertimeForPerson(Person person) {

        Assert.notNull(person, "Person to get left overtime for must be given.");

        BigDecimal totalOvertime = getTotalOvertimeForPerson(person);
        BigDecimal overtimeReduction = applicationService.getTotalOvertimeReductionOfPerson(person);

        return totalOvertime.subtract(overtimeReduction);
    }


    private BigDecimal getTotalOvertimeForPerson(Person person) {

        Optional<BigDecimal> totalOvertime = Optional.ofNullable(overtimeDAO.calculateTotalHoursForPerson(person));

        return totalOvertime.orElse(BigDecimal.ZERO);

    }
}
