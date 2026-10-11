# Visual Regression Tests

Visual regression tests take screenshots of the running application and compare them against committed baselines.
For this to work, every run must render exactly the same pixels. This document collects the decisions made to get
there and the open points for future improvements.

## Scope

* Tests verify **read-only pages** only. Forms may be opened and filled in for a screenshot, but are never submitted.
* Tests run locally and in CI.

## Starting the application

```bash
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=demodata,visualtest"
```

The `visualtest` profile (`application-visualtest.yaml`) is meant to be used together with `demodata`.

## Decisions

### 1. Fixed application time

All demo data (applications for leave, sick notes, overtime, accounts, working times) is created relative to
`LocalDate.now(clock)`, using the application-wide `Clock` bean. The `visualtest` profile freezes this clock via
`uv.development.clock.fixed-instant` (see `ClockConfig` and `ClockProperties`).

* `Clock.fixed` is used instead of `Clock.offset`. With `offset`, timestamps like comment creation times would differ by
  seconds on every run.
* The frozen clock only affects code that injects the `Clock` bean. Infrastructure keeps using the real time: servlet
  sessions, OIDC/JWT token validation, ShedLock and the cron triggers of the `TaskScheduler`. So login and sessions
  work as usual.
* The pivot `2026-09-16T09:00:00Z` is a wednesday with no public holidays in the default federal state
  (Baden-Württemberg) within the demo data range (today −40 days up to +16 days), and all demo applications for leave
  stay within the current year. Otherwise `DurationChecker` would silently skip some of them. Holidays never break
  stability though - a different pivot only changes how much demo data is visible, not whether it is stable.
* 09:00 UTC is far enough from midnight, so 'today' is the same date in every relevant timezone.

### 2. Deterministic demo data

* Additional demo persons get a personnel number derived from their email (`DemoDataCreationService#personnelNumberOf`)
  instead of a random one.
* Demo persons are created sequentially and the demo data listener (`PersonCreatedEventListener`) is synchronous, so
  database ids are the same on every run against a fresh database. The only `@Async` listeners (`calendar` package)
  are no-ops for demo data.

### 3. Fresh database per run

Demo data is only created for persons without existing data. A reused database would keep data of previous runs.
Therefore every run starts with an empty database (e.g. `docker compose down -v` or Testcontainers). Since tests are
read-only, all tests can share one database and run in parallel and in any order. No reset between tests is needed.

### 4. Frozen browser time

The frontend uses the browser time as well, e.g. the person overview calendar is centered on `new Date()`
(`js/person/overview.js`), and the calendar and datepicker highlight 'today'. Without freezing the browser time, the
calendar would show other months than the server data.

* Playwright freezes the browser time with `page.clock.setFixedTime(...)`.
* Server and browser must use the **same instant**. Use one environment variable as the single source, feeding both
  `UV_DEVELOPMENT_CLOCK_FIXEDINSTANT` and the Playwright configuration.
* The browser timezone and locale are fixed as well (`timezoneId: "Europe/Berlin"`, `locale: "de-DE"`), so the CI
  runner settings do not change date formats.

### 5. No scheduled jobs during test runs

Cron jobs run on real time and could mutate data while screenshots are taken (e.g. reminder notifications daily at
06:00 and 07:00). The `visualtest` profile has to disable them:

* `uv.sick-note.end-of-pay-notification.cron`
* `uv.application.reminder-notification.cron`
* `uv.application.upcoming-notification.cron`
* `uv.application.upcoming-holiday-replacement-notification.cron`
* `uv.account.update.cron` (turn of the year)
* `uv.account.vacation-days-reminder.vacation-days-left-cron`
* `uv.account.vacation-days-reminder.expired-remaining-vacation-days-cron`

Spring's usual way to disable a cron task, `"-"`, is **rejected** here: the properties are validated with
`@CronExpression`, which delegates to `CronExpression.isValidExpression` and fails for `"-"`. Options, to be decided:

* use a valid expression that never fires, e.g. `0 0 0 30 2 *` (30th of february) - to be verified that Spring parses it
  and simply never schedules it
* or allow `"-"` in `CronExpressionConstraintValidator`

### 6. Wait for readiness

Demo data is created on `LivenessState.CORRECT`, while Tomcat is already accepting requests. Tests must not start before
the readiness probe `/actuator/health/readiness` (management port 8081) reports `ACCEPTING_TRAFFIC`. Outside Kubernetes
the probes may have to be enabled with `management.endpoint.health.probes.enabled: true`.

### 7. Rendering environment

* Baselines are generated **only in a pinned container** (e.g. the official Playwright Docker image), locally as well
  as in CI. Body text uses a system font stack, so macOS and Linux render differently. Never commit baselines created
  natively on a developer machine.
* Playwright `toHaveScreenshot` disables CSS animations and transitions and hides the caret by default, and waits for
  web fonts to be loaded.
* Gravatar is disabled by default (`AvatarSettings`), so avatars are rendered as SVG initials without external
  requests.

## Status

| Topic                                       | Status          |
|---------------------------------------------|-----------------|
| Fixed application clock (`visualtest`)      | implemented     |
| Deterministic personnel numbers             | implemented     |
| Disable cron jobs in `visualtest` profile   | open (see 5.)   |
| Enable readiness probe if needed            | open            |
| Playwright setup (clock, timezone, locale)  | open            |
| Pinned container for baselines and CI       | open            |
| Fresh database per CI run                   | open            |

## Future improvements

* **Secondary sort key for lists.** Lists sorted by e.g. date without a secondary sort key return entries with equal
  values in an order Postgres does not guarantee. With identical fresh databases this is stable in practice, but not
  guaranteed. When a flaky diff shows up, add a secondary sort (e.g. by id) to the affected query.
* **Server provided 'today' for the frontend.** Instead of mocking the browser clock, the server could pass its 'today'
  to the frontend (e.g. `globalThis.uv.today`), and the JavaScript would use it instead of `new Date()`. This would
  also avoid client/server timezone mismatches in production, but touches several components.
* **Self-hosted body font.** Would make local and CI rendering more similar. Optional, the pinned container is enough.
* **Remaining `now()` calls without clock.** `BackupDataCollectionService` uses `LocalDate.now()` and domain events use
  `Instant.now()`. Not visible in the UI today, but they ignore the fixed clock.
