package org.synyx.urlaubsverwaltung;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

/**
 * Base for integration tests that guard a job against N+1 queries: counts every SQL statement Hibernate prepares
 * while a job runs, including the statements of the {@code @Async} mail sending, and the mails sent.
 * <p>
 * The {@code user_settings} lookup is not counted: {@code MailServiceImpl} resolves the locale of the recipients once
 * per mail, which is expected to grow with the number of mails.
 */
@SpringBootTest(properties = {
    "spring.jpa.properties.hibernate.session_factory.statement_inspector=org.synyx.urlaubsverwaltung.QueryCountTestSupport$Counter",
    "management.health.mail.enabled=false",
    // a thread pool instead of virtual threads, so that the @Async work can be awaited
    "spring.threads.virtual.enabled=false",
    "spring.task.execution.pool.core-size=8",
    "spring.task.execution.thread-name-prefix=query-count-async-",
})
@Import(QueryCountTestSupport.ClockConfig.class)
public abstract class QueryCountTestSupport extends SingleTenantTestContainersBase {

    protected static final LocalDate TODAY = LocalDate.of(2026, 5, 4);

    private static final String ASYNC_THREAD_PREFIX = "query-count-async-";

    private static final Queue<String> STATEMENTS = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger MAILS = new AtomicInteger();
    private static volatile boolean counting = false;

    public static class Counter implements StatementInspector {
        @Override
        public String inspect(String sql) {
            final String threadName = Thread.currentThread().getName();
            if (counting && (threadName.equals("main") || threadName.startsWith(ASYNC_THREAD_PREFIX))) {
                STATEMENTS.add(sql);
            }
            return sql;
        }
    }

    @TestConfiguration
    public static class ClockConfig {
        @Bean
        @Primary
        public Clock fixedClock() {
            return Clock.fixed(TODAY.atTime(8, 0).toInstant(UTC), UTC);
        }
    }

    protected record Measurement(int statements, int mails) {
    }

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Autowired
    @Qualifier("applicationTaskExecutor")
    private ThreadPoolTaskExecutor asyncExecutor;
    @Autowired
    private Clock clock;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PersonService personService;
    @Autowired
    private DepartmentService departmentService;

    @BeforeEach
    void countMails() {
        assertThat(LocalDate.now(clock)).isEqualTo(TODAY);
        doAnswer(_ -> MAILS.incrementAndGet()).when(javaMailSender).send(any(MimeMessage.class));
        doAnswer(_ -> MAILS.incrementAndGet()).when(javaMailSender).send(any(SimpleMailMessage.class));
        when(javaMailSender.createMimeMessage()).thenAnswer(_ -> new MimeMessage((Session) null));
        truncatePersonData();
    }

    @AfterEach
    void cleanUp() {
        awaitAsyncWork();
        truncatePersonData();
    }

    protected Measurement measure(Runnable job) {
        awaitAsyncWork();
        STATEMENTS.clear();
        MAILS.set(0);
        counting = true;
        try {
            job.run();
            awaitAsyncWork();
        } finally {
            counting = false;
        }

        final int statements = (int) STATEMENTS.stream()
            .filter(sql -> !sql.toLowerCase(Locale.ROOT).contains("from user_settings"))
            .count();
        return new Measurement(statements, MAILS.get());
    }

    protected void truncatePersonData() {
        // cascades to every table referencing a person or a department
        jdbcTemplate.execute("TRUNCATE person, department CASCADE");
    }

    protected Person createPerson(String username, List<Role> roles) {
        final List<MailNotification> notifications = Arrays.stream(MailNotification.values())
            .filter(mailNotification -> mailNotification.isValidWith(roles))
            .toList();
        return personService.create(username, "First-" + username, "Last-" + username, username + "@example.org", notifications, roles);
    }

    /**
     * Creates one OFFICE and one BOSS person and three departments, each with a dedicated department head, a
     * dedicated second stage authority and the given number of plain members.
     *
     * @return all created persons, the plain members first
     */
    protected Staff createStaff(int membersPerDepartment) {

        final Person office = createPerson("office", List.of(USER, OFFICE));
        final Person boss = createPerson("boss", List.of(USER, BOSS));

        final List<Person> members = new ArrayList<>();
        final List<Person> managers = new ArrayList<>();
        for (int departmentNumber = 0; departmentNumber < 3; departmentNumber++) {
            final Person departmentHead = createPerson("head" + departmentNumber, List.of(USER, DEPARTMENT_HEAD));
            final Person secondStageAuthority = createPerson("ssa" + departmentNumber, List.of(USER, SECOND_STAGE_AUTHORITY));
            managers.add(departmentHead);
            managers.add(secondStageAuthority);

            final List<Person> departmentMembers = new ArrayList<>();
            for (int memberNumber = 0; memberNumber < membersPerDepartment; memberNumber++) {
                departmentMembers.add(createPerson("member" + departmentNumber + "-" + memberNumber, List.of(USER)));
            }
            members.addAll(departmentMembers);

            final Department department = new Department();
            department.setName("department-" + departmentNumber);
            department.setMembers(departmentMembers);
            department.setDepartmentHeads(List.of(departmentHead));
            department.setSecondStageAuthorities(List.of(secondStageAuthority));
            department.setTwoStageApproval(true);
            departmentService.create(department);
        }

        return new Staff(office, boss, managers, members);
    }

    protected record Staff(Person office, Person boss, List<Person> managers, List<Person> members) {

        public List<Person> all() {
            final List<Person> all = new ArrayList<>(members);
            all.addAll(managers);
            all.add(office);
            all.add(boss);
            return all;
        }
    }

    private void awaitAsyncWork() {
        final long deadline = System.nanoTime() + Duration.ofSeconds(60).toNanos();
        int stableChecks = 0;
        int lastMails = -1;
        int lastStatements = -1;
        while (stableChecks < 5) {
            if (System.nanoTime() > deadline) {
                throw new IllegalStateException("async work did not finish in time");
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
            final boolean idle = asyncExecutor.getActiveCount() == 0 && asyncExecutor.getThreadPoolExecutor().getQueue().isEmpty();
            final int mails = MAILS.get();
            final int statements = STATEMENTS.size();
            stableChecks = idle && mails == lastMails && statements == lastStatements ? stableChecks + 1 : 0;
            lastMails = mails;
            lastStatements = statements;
        }
    }
}
