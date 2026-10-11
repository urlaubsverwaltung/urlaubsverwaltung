# Visual Regression Tests

Visual regression tests take screenshots of the running application and compare them against committed baselines.
For this to work, every run must render exactly the same pixels. This document collects the decisions made to get
there and the open points for future improvements.

## Scope

* Tests verify **read-only pages** only. Forms may be opened and filled in for a screenshot, but are never submitted.
* Tests run locally and in CI, in parallel.
* Variants: desktop and mobile, light theme first. The dark theme is used and **must be covered** as well, it follows
  once the setup runs reliably.

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

### 8. Page discovery and manifest

The pages to verify are not maintained by hand. A crawler discovers them, and a committed manifest acts as golden
master - just like snapshot tests.

* **Manifest**, one file per role (e.g. `pages/office.json`), maps a URL pattern to an example URL and a status:
  ```json
  {
    "/web/application/{id}": { "url": "/web/application/42", "status": "covered" },
    "/web/sicknote/{id}/edit": { "status": "ignored", "reason": "..." }
  }
  ```
  One file per role, so parallel discovery tests never write the same file.
* **Screenshot tests** are generated from the manifest when the test files are loaded: one test per role × page ×
  variant. Playwright cannot add tests during a run, so crawling and screenshotting in the same test would end up as
  one giant test that stops at the first diff.
* **Discovery test** crawls the application per role and compares the result with the manifest.
* Crawling rules:
  * crawl per role (user, departmentHead, secondStageAuthority, boss, office, admin) - each role sees other pages
  * only `<a href>` links; breadth-first with a sorted queue and a depth limit, so the crawl is deterministic
  * ids in paths are collapsed into patterns (`/web/person/5/overview` → `/web/person/{id}/overview`), the example URL
    is chosen deterministically (first one in sorted order)
  * query parameters are dropped (`/web/person/5/overview?year=2025` → `/web/person/{id}/overview`). They filter
    data (year, month, department, sorting, pagination), which is irrelevant for visual regression tests. Example
    URLs in the manifest have no query parameters either
  * excluded: logout, external links, downloads (ICS, CSV), `/api`, actuator, and GET links that mutate data (to be
    checked whether there are any)
* Not discoverable by the crawler: dialogs, tabs, popovers, tooltips, validation errors and GET filter forms. These
  states need a few hand-written tests.

### 9. Update mode and blocking behaviour

There is no separate update switch. The discovery test reads Playwright's `test.info().config.updateSnapshots`, so
`-u` updates screenshots and the manifest at once.

| Mode             | Screenshots           | Manifest                                         |
|------------------|-----------------------|--------------------------------------------------|
| `none`           | compare only          | new or removed pages → fail                      |
| `missing`        | write new baselines   | add new pages (and fail), report removed pages   |
| `-u` / `changed` | update changed ones   | add new pages, remove stale ones                 |
| `all`            | rewrite all           | rebuild the manifest from scratch                |

* Discovery is **blocking** in both directions, like missing snapshots in Playwright: new pages fail, removed pages
  fail. The fix is always `-u` and commit.
* Playwright does not detect orphaned baseline PNGs. Discovery reports PNGs no manifest entry refers to as well.
* CI must use `updateSnapshots: process.env.CI ? 'none' : 'missing'`. Playwright's default `missing` would silently
  accept new baselines in CI.
* Pages added by discovery get their baselines only in the next run, since screenshot tests are collected from the
  manifest before discovery runs. To be verified whether `globalSetup` runs before test collection, otherwise an npm
  script runs `-u` twice.

### 10. Parallel execution

Read-only tests on a shared database can run in any order and in parallel (`fullyParallel: true`). Playwright
distributes single tests to free workers, no manual bucketing.

* **Projects:**
  ```
  setup               → logs in every role once via Keycloak, writes .auth/<role>.json
  <role>-<variant>    → depends on setup, uses storageState .auth/<role>.json,
                        generates tests from pages/<role>.json
  discovery           → depends on setup, crawls per role
  ```
  Projects share the worker pool. They give a clean per role config, selective runs (`--project=office-desktop`) and
  separated screenshot paths. Discovery is no dependency of the screenshot projects, both run in parallel.
* Logging in once per role is faster, does not flood Keycloak and moves possible writes on login (person sync) out of
  the parallel phase.
* To be checked: whether the server session holds UI state (selected year, filters). Then parallel tests of one role
  would influence each other and need a login per worker.
* To be checked: lazy creation on GET (e.g. a holiday account for another year). Two workers could create the same
  data concurrently → duplicates or constraint violations. Trigger it once in the setup project or fix it in the
  application.
* The number of workers is limited by the single application instance and Postgres (default Hikari pool: 10
  connections). Start with 4-6 workers.
* Sharding across CI machines (`--shard`) is postponed. Every shard needs its own application and database including
  demo data creation, which only pays off for long runs.

### 11. Variants

* Start with **desktop and mobile, light theme**, as variants per role project.
* **Dark theme must be covered** at the end, as a third variant once the setup is stable.
* The theme is a user setting (`system`, `light`, `dark`). With `system`, `prefers-color-scheme` decides
  (`_layout.html`). So the variants can use Playwright's `colorScheme: 'light' | 'dark'` without changing user
  settings in the database - assuming the demo persons use `system`, to be verified. Set `colorScheme` explicitly in
  any case.

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
| Page discovery and manifest                 | open            |
| Projects per role and variant, setup login  | open            |
| Desktop and mobile, light theme             | open            |
| Dark theme                                  | open, required  |

## Future improvements

* **Secondary sort key for lists.** Lists sorted by e.g. date without a secondary sort key return entries with equal
  values in an order Postgres does not guarantee. With identical fresh databases this is stable in practice, but not
  guaranteed. When a flaky diff shows up, add a secondary sort (e.g. by id) to the affected query.
* **Server provided 'today' for the frontend.** Instead of mocking the browser clock, the server could pass its 'today'
  to the frontend (e.g. `globalThis.uv.today`), and the JavaScript would use it instead of `new Date()`. This would
  also avoid client/server timezone mismatches in production, but touches several components.
* **Query parameter allowlist.** Should a query parameter ever change the layout instead of filtering data, the
  crawler config could get an allowlist per pattern with a fixed value, resulting in an additional manifest entry.
* **Self-hosted body font.** Would make local and CI rendering more similar. Optional, the pinned container is enough.
* **Remaining `now()` calls without clock.** `BackupDataCollectionService` uses `LocalDate.now()` and domain events use
  `Instant.now()`. Not visible in the UI today, but they ignore the fixed clock.
