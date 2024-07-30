package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@BenchmarkMode(Mode.Throughput)
@Fork(3)
@Warmup(iterations = 5, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
public class ICalServiceBenchmarkTest {

    @State(Scope.Thread)
    public static class ICalServiceState {
        public final ICalService iCalService = new ICalService(new CalendarProperties());
        public final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        public final List<Absence> absence = List.of(
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings())),
            new Absence(person, new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), DayLength.FULL), new AbsenceTimeConfiguration(new TimeSettings()))
        );
    }

    @Benchmark
    public static ByteArrayResource benchmark(final ICalServiceState state) {
        return state.iCalService.getCalendar("Some title", state.absence, state.person);
    }

    @Test
    @Tag("BenchmarkTest")
    void runJmhBenchmark() throws RunnerException {
        final Options opt = new OptionsBuilder()
            .include(ICalServiceBenchmarkTest.class.getSimpleName())
            .build();

        final Collection<RunResult> runResults = new Runner(opt).run();
        assertThat(runResults).isNotEmpty();
    }
}
