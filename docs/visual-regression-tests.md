# Visual Regression Tests

Visual regression tests take screenshots of the running application and compare them against committed baselines.
For this to work, every run must render exactly the same pixels. This document collects the decisions made to get
there and the open points for future improvements.

## Scope

* Tests verify **read-only pages** only. Forms may be opened and filled in for a screenshot, but are never submitted.
* Tests run locally and in CI, in parallel, and are always triggered manually.
* Browser: Chromium for now. Firefox and WebKit may become a requirement, so no browser specific solutions.
* Variants: desktop and mobile in the light theme, desktop in the dark theme.

## Running the tests

Requirements: Docker and Git LFS (`git lfs install` once, then `git lfs pull` to get the baselines).

### With Maven

```bash
./mvnw verify -Pvisual-test                          # compare
./mvnw verify -Pvisual-test -Dvisual-test.args=-u    # accept new and changed pages and screenshots
```

The profile `visual-test` does everything in one go:

| Phase                   | What                                                                                   |
|-------------------------|----------------------------------------------------------------------------------------|
| (like `ui-test`)        | skips unit tests, integration tests, JavaScript linting and tests                      |
| `package`               | `docker compose down -v`, also after a killed previous run; resets the last result     |
| `pre-integration-test`  | `spring-boot:start` with `demodata,visualtest`, waits until ready (demo data created)  |
| `integration-test`      | `npm run test:visual`, failures do not stop the build yet                              |
| `post-integration-test` | `spring-boot:stop`, graceful shutdown removes compose containers and volumes           |
| `verify`                | fails the build if the playwright run failed (`target/visual-tests/results/.last-run.json`) |

Failures must not stop the build in `integration-test`, otherwise Maven would skip `spring-boot:stop` and the
application and containers would keep running. A full run takes about 4 minutes, including build and startup.

### Iterating locally

Maven builds and starts the application on every run. While working on the UI, keep the application running and run
the tests directly:

```bash
docker compose down -v
./mvnw spring-boot:run -Dspring-boot.run.profiles=demodata,visualtest

# once the application is ready (http://localhost:8081/actuator/health/readiness)
npm run test:visual            # compare
npm run test:visual -- -u      # accept new and changed pages and screenshots
```

* The browser server in the pinned container is started by playwright (`webServer`) and reused if already running.
* Run a single variant or role: `npm run test:visual -- --project=desktop --grep office`
* Another application instance, e.g. in a git worktree: start the compose services once (`docker compose up -d`) and
  every instance with `spring.docker.compose.enabled=false`, explicit datasource properties and its own
  `server.port`/`management.server.port`, then `UV_BASE_URL=http://localhost:8180 npm run test:visual`. The instances
  share postgres (demo data, sessions) and keycloak (any redirect uri is allowed). Start one instance on a fresh
  database first, the others only once its demo data is created.
* Report with expected, actual and diff images: `npx playwright show-report target/visual-tests/report`
* New pages need two runs with `-u`: the first adds them to the manifest, the second creates their screenshots (see
  10.).
* Stopping the application removes the compose containers and volumes. A visual test run wipes the local development
  database.

The `visualtest` profile (`application-visualtest.yaml`) is meant to be used together with `demodata`.
`spring-boot-docker-compose` detects the existing `docker-compose.yml` and starts Postgres, Mailpit and Keycloak
(including the realm import with the demo users) before the application. No separate compose file is needed.

### Files

| Path                                   | Content                                                          |
|----------------------------------------|------------------------------------------------------------------|
| `playwright.config.mjs`                | remote browser, projects `setup`, `discovery`, `desktop`, `mobile`, `desktop-dark` |
| `pom.xml`, profile `visual-test`       | maven entry point                                                |
| `src/test/visual/browser-server.mjs`   | browser server in the container, image tag from `package.json`, started by playwright `webServer` |
| `src/test/visual/screenshots.teardown.mjs` | orphaned screenshots and name collisions, after all screenshot tests |
| `src/test/visual/verify-last-run.mjs`  | fails the maven `verify` phase if the playwright run failed      |
| `src/test/visual/auth.setup.mjs`       | logs in every demo role once, sessions in `target/visual-tests/auth` |
| `src/test/visual/discovery.spec.mjs`   | crawler, compares with the manifests                             |
| `src/test/visual/pages.spec.mjs`       | screenshot tests generated from the manifests                    |
| `src/test/visual/pages/<role>.json`    | manifests, golden master of the pages                            |
| `src/test/visual/__screenshots__/`     | baselines (Git LFS), `<project>/<role>/<page>.png`               |
| `.github/workflows/visual-regression-tests.yml` | manual CI workflow                                      |

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
Therefore every run starts with an empty database. Since tests are read-only, all tests can share one database and run
in parallel and in any order. No reset between tests is needed.

* Spring's docker compose support runs `docker compose stop` on shutdown by default, which keeps the Postgres container
  and its (anonymous) volume. The `visualtest` profile configures `spring.docker.compose.stop.command: down` with
  `stop.arguments: -v`. Verified: containers and volumes are removed on shutdown.
* A killed application (cancelled CI run, hard kill) skips the shutdown hook, so the Maven profile runs
  `docker compose down -v` beforehand as well.
* Verified: after a restart with an empty database, all screenshots match without any update.
* It is the same compose project as for local development: a visual test run wipes the local development database,
  and both cannot run at the same time (ports 5434, 8090). This is accepted.

### 4. Frozen browser time

The frontend uses the browser time as well, e.g. the person overview calendar is centered on `new Date()`
(`js/person/overview.js`), and the calendar and datepicker highlight 'today'. Without freezing the browser time, the
calendar would show other months than the server data.

* Playwright freezes the browser time with `page.clock.setFixedTime(...)`.
* Server and browser must use the **same instant**. The single source is `application-visualtest.yaml`: the Playwright
  fixture reads `fixed-instant` from it (`environment.mjs`), so there is no environment variable to keep in sync.
* The browser timezone and locale are fixed as well (`timezoneId: "Europe/Berlin"`, `locale: "de-DE"`), so the CI
  runner settings do not change date formats.

### 5. Scheduled jobs need no handling

Cron jobs run on real time, so they could fire during a test run. They were checked and need no handling:

| Job                                    | Schedule        | Writes                                              |
|----------------------------------------|-----------------|-----------------------------------------------------|
| EndOfSickPayNotification               | daily 06:00     | `endOfSickPayNotificationSend` on the sick note     |
| WaitingApplicationsReminderNotification| daily 07:00     | `remindDate` on the application                     |
| UpcomingApplicationsReminderNotification| daily 07:00    | `upcomingApplicationsReminderSend`                  |
| UpcomingHolidayReplacementReminder     | daily 07:00     | `upcomingHolidayReplacementNotificationSend`        |
| NotifyForExpiredRemainingVacationDays  | daily 06:00     | `expiryNotificationSentDate` on the account         |
| RemindForCurrentlyLeftVacationDays     | 1st of october  | nothing                                             |
| UpdateAccountsForNextPeriod            | 1st of january  | holiday accounts of the next year                   |

* The mail jobs only write 'already sent' markers. None of them is rendered in templates or view controllers, so
  screenshots are not affected. Without a mail server the jobs log errors, which is irrelevant as well.
* Only the turn of the year job has visible effects, and it fires on january 1st at 05:00 only.
* Tests are triggered manually only (see 14.), so the risk is negligible.

Re-check this if a job starts writing fields that are rendered. Disabling jobs would not be trivial: Spring's `"-"`
is rejected by the `@CronExpression` validation of the cron properties.

### 6. Wait for readiness

Demo data is created on `LivenessState.CORRECT`, while Tomcat is already accepting requests. Tests must not start before
the readiness probe `/actuator/health/readiness` (management port 8081) reports `UP`. Outside Kubernetes the probes are
disabled by default, the `visualtest` profile enables them with `management.endpoint.health.probes.enabled: true`.
The Maven profile does not need the probe: `spring-boot:start` waits until the application is ready, which is after the
demo data has been created. When running `npm run test:visual` against a manually started application, nothing checks readiness:
wait for the probe yourself, otherwise the logins may run against an application still creating demo data.

### 7. Rendering environment

Font rendering differs between operating systems (antialiasing, hinting, font fallback, scrollbars), and even between
Linux distributions with different fonts installed. Pixel exact comparison only works if every screenshot is rendered
on the same platform - while the UI is developed on macOS, and CI and most colleagues use Linux.

* **The browser always runs in a pinned Linux container** (official Playwright Docker image, pinned version) via
  `npx playwright run-server`. The test runner runs natively and connects via `connectOptions.wsEndpoint`.
* **`exposeNetwork: '<loopback>'`** tunnels the remote browser's `localhost` requests to the machine of the test runner.
  So `localhost:8080` (application) and the OIDC redirect to `localhost:8090` (Keycloak) work, without host networking
  and without browser specific flags. It is a Playwright feature and works for Firefox and WebKit as well.
* The application keeps running natively via `mvn spring-boot:run`, so UI changes only need an application restart.
* The configuration always uses `connectOptions` and **never falls back to a local browser**, so nobody can create
  baselines on macOS by accident.
* `{platform}` is removed from `snapshotPathTemplate`: one baseline set for everyone instead of one per OS.
* CI uses the same remote browser setup.
* Verified: the Keycloak login round trip works through `exposeNetwork` (redirect to `localhost:8090` and back), the
  user agent reports `X11; Linux x86_64`.
* **Plan B** (not needed, kept for reference), if `exposeNetwork` stops working: run everything in containers (application from the built jar,
  Postgres, Keycloak, browser, test runner) with a separate compose file, all containers sharing one network namespace
  (`network_mode: "service:…"`), so `localhost` is the same for everyone. Hermetic, but needs a jar and image build for
  every UI change and is a second way of starting the application.
* Rejected: Docker host networking (needs opt-in on macOS Docker Desktop), Chromium `--host-resolver-rules` (browser
  specific), baselines generated by CI only (slow feedback for UI development).
* Playwright `toHaveScreenshot` disables CSS animations and transitions and hides the caret by default, and waits for
  web fonts to be loaded.
* The first baselines revealed a bug: the calendar toggled the style of its buttons on every media query `change` event
  instead of following the media query state. Chromium fires such an event while capturing a full page screenshot,
  so the calendar width changed on every screenshot. Fixed in the application (`calendar.js`), no screenshot specific
  CSS needed.
* Gravatar is disabled by default (`AvatarSettings`), so avatars are rendered as SVG initials without external
  requests.

### 8. Fixed application version

The footer renders the application version (`_footer.html`, `info.app.version`), which changes with every release
and would break every screenshot. The `visualtest` profile sets the hard coded version `info.app.version: 1.33.7`.
This covers every place the version is rendered (footer, API docs) without masking. The backup extension sees the fake
version as well, which is irrelevant in a test profile.

### 9. Page discovery and manifest

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
  * ids in paths are collapsed into patterns (`/web/person/5/overview` → `/web/person/{id}/overview`). The example URL
    is the first URL of the pattern the crawl loads successfully - deterministic, since the links of every page are
    sorted and the crawl is breadth-first. Not the smallest URL of the pattern overall.
  * a pattern counts as discovered only once one of its URLs loaded. If a URL fails (error, non-OK status, not HTML),
    other URLs of the same pattern are still tried, so a single failing URL does not drop the page from the manifest
  * query parameters are dropped (`/web/person/5/overview?year=2025` → `/web/person/{id}/overview`). They filter
    data (year, month, department, sorting, pagination), which is irrelevant for visual regression tests. Example
    URLs in the manifest have no query parameters either
  * excluded: external links, downloads (`*/download`, `*/export`, ICS feeds `persons/{id}/calendar`), `/api`,
    actuator, google handshake. Logout is a POST anyway.
  * redirects are recorded with their target (`/web/overview` → `/web/person/{id}/overview`)
* Verified: a full crawl writes nothing but `spring_session` rows (compared `pg_stat_user_tables` before and after), so
  no GET link mutates data and nothing is created lazily.
* Initial manifests: user 21, departmentHead 24, secondStageAuthority 27, boss 27, office 46, admin 16 pages - 161 per
  variant, 322 screenshots.
* Not discoverable by the crawler: dialogs, tabs, popovers, tooltips, validation errors and GET filter forms. These
  states need a few hand-written tests.

### 10. Update mode and blocking behaviour

There is no separate update switch. The discovery test reads Playwright's `test.info().config.updateSnapshots`, so
`-u` updates screenshots and the manifest at once.

| Mode             | Screenshots         | Manifest (discovery)                      | Orphaned screenshots (teardown) |
|------------------|---------------------|-------------------------------------------|---------------------------------|
| `none`           | compare only        | new or removed pages → fail               | fail                            |
| `missing`        | write new baselines | add new pages; new or removed pages → fail | fail                           |
| `-u` / `changed` | update changed ones | add new pages, remove stale ones          | delete                          |
| `all`            | rewrite all         | rebuild, keeps `ignored` decisions        | delete                          |

* Discovery is **blocking** in both directions, like missing snapshots in Playwright: new pages fail, removed pages
  fail. The fix is always `-u` and commit.
* Playwright does not detect orphaned baseline PNGs. The teardown project `screenshots` (`screenshots.teardown.mjs`)
  reports PNGs no manifest entry refers to, and deletes them with `-u`. It runs as teardown of `setup`, i.e. after
  discovery and all screenshot tests: a screenshot test of a page discovery has just removed could otherwise write
  its baseline again after it was deleted.
* Playwright replaces everything but letters, digits and `-` in screenshot file names (`/web/person/{id}/overview` →
  `web-person-id-overview.png`). Two patterns mapping to the same name fail the teardown, even with `-u`.
* CI must use `updateSnapshots: process.env.CI ? 'none' : 'missing'`. Playwright's default `missing` would silently
  accept new baselines in CI.
* Pages added by discovery get their baselines only in the next run, since screenshot tests are collected from the
  manifest before discovery runs. So adding pages needs `-u` twice. Moving discovery into `globalSetup` might avoid
  this, not verified.

### 11. Parallel execution

Read-only tests on a shared database can run in any order and in parallel (`fullyParallel: true`). Playwright
distributes single tests to free workers, no manual bucketing.

* **Projects:**
  ```
  setup       → logs in every role once via Keycloak, writes target/visual-tests/auth/<role>.json
  discovery   → depends on setup, crawls per role
  desktop     → depends on setup, one describe block per role with its storageState,
  mobile        tests generated from pages/<role>.json
  desktop-dark
  screenshots → teardown of setup, runs after all of the above: orphaned screenshots, name collisions
  ```
  Implemented with one project per variant and roles as describe blocks instead of one project per role and variant:
  Playwright cannot read the project at collection time, so per role projects would need one spec file per role.
  Selective runs: `--project=desktop --grep office`. Discovery is no dependency of the screenshot projects.
* The logins of the setup project run **serially**: parallel first logins against a freshly started Keycloak fail with
  "login attempt timed out". The tests themselves stay parallel.
* Logging in once per role is faster, does not flood Keycloak and moves possible writes on login (person sync) out of
  the parallel phase.
* Verified: the session holds only the security context and the CSRF token, no UI state. All tests of a role share
  one login.
* Verified: no lazy creation on GET (see 9.).
* 4 workers, limited by the single application instance and the browser container. The tests take about 2.5 minutes.
* Sharding across CI machines (`--shard`) is postponed. Every shard needs its own application and database including
  demo data creation, which only pays off for long runs.

### 12. Variants

* **desktop and mobile in the light theme, desktop in the dark theme**, as one project per variant (see 11.).
* The dark theme only changes colors, not the layout, so a mobile dark variant would add screenshots without
  covering more.
* The theme is a user setting (`system`, `light`, `dark`). With `system`, `prefers-color-scheme` decides
  (`_layout.html`). Verified: the default is `system` (`UserSettings.DEFAULT`), so the variants can use Playwright's
  `colorScheme: 'light' | 'dark'` without changing user settings in the database. `colorScheme: 'light'` is set
  explicitly, `desktop-dark` overrides it with `'dark'`.

### 13. Baselines in Git LFS

Baseline PNGs are stored in Git LFS. A rough estimate (not measured): 150-250 role × page combinations, two variants
now, three with dark theme, 100-400 KB per full page screenshot - up to several hundred MB per full set, and every
global UI change rewrites nearly all of them. In plain git, the repository would grow permanently.

* Track the snapshot directory via `.gitattributes` before the first baseline is committed. Moving baselines to LFS
  later requires rewriting history.
* Regular CI builds check out without `lfs: true` and get pointer files only, no LFS bandwidth is used.
* Contributors without `git-lfs` get pointer files as well, which is harmless since the tests are not part of the
  regular build.
* `core.hooksPath` points to the tracked `.githooks`, so git lfs cannot install its `pre-push` hook. `.githooks/pre-push`
  calls `git lfs pre-push` and is skipped without git-lfs. Without it, a push would upload pointers but no PNGs.
* The initial 322 baselines are about 28 MB, the 161 dark baselines another 18 MB.

### 14. Manual execution only

Visual regression tests never run automatically.

* CI: `.github/workflows/visual-regression-tests.yml` with `workflow_dispatch` only. Not part of `build.yml`, not
  triggered by pull requests or pushes. It checks out with LFS, runs `./mvnw verify -Pvisual-test` with `CI=true`
  (`updateSnapshots: 'none'`, no reuse of a running browser server) and uploads report and results as artifact.
  Baselines are created locally with `-u`, the pixels are the same since the browser runs in the same container.
* Local: only by an explicit command. Not part of the default Maven build (profile `visual-test`) and not part of
  `npm test` (vitest must not pick up the Playwright specs).
* Discovery is blocking for the manual run, not for pull requests.

### 15. Location in the repository

* `@playwright/test` is a devDependency of the existing `package.json`, the tests live in `src/test/visual/`, started
  via an npm script like `test:visual`. One Node project for a better developer experience.
* The normal build stays unaffected apart from installing the package: `npm run lint` and vitest only cover
  `src/main/javascript`. Verified: `npm install` does not download browsers (they live in the container anyway).
* The version is pinned exactly, like all other dependencies: `1.63.0` (1.64.0 was released only days before).
  Client and container image must use the same Playwright version, so `browser-server.mjs` derives the image tag
  (`mcr.microsoft.com/playwright:v<version>-noble`) from `package.json`. A dependency update moves both.

### 16. Screenshot settings

* **Full page** (`fullPage: true`). Regressions below the fold count as well, the size is handled by Git LFS.
* **Playwright defaults for comparison**, no additional global tolerance. Pinned container, frozen time and fixed data
  exist to make exact comparison work. Tolerance only per page if it turns out flaky, never globally.
* **Waiting:** Playwright's built-in stability check (two identical consecutive screenshots) plus `networkidle` by
  default. Page specific waits (e.g. calendar days rendered) only where needed.

## Status

| Topic                                       | Status                         |
|---------------------------------------------|--------------------------------|
| Fixed application clock (`visualtest`)      | implemented                    |
| Deterministic personnel numbers             | implemented                    |
| Fixed version `1.33.7` in `visualtest`      | implemented                    |
| Fresh database (compose `down -v`)          | implemented, verified          |
| Readiness probe                             | implemented, verified          |
| Remote browser in pinned container          | implemented                    |
| Keycloak login via `exposeNetwork`          | verified                       |
| Playwright setup (clock, timezone, locale)  | implemented                    |
| Page discovery and manifest                 | implemented                    |
| Screenshot tests, desktop and mobile light  | implemented, 322 baselines     |
| Screenshot tests, desktop dark              | implemented, 161 baselines     |
| Git LFS for baselines, pre-push hook        | implemented                    |
| Manual CI workflow (`workflow_dispatch`)    | implemented, not yet run on CI |
| Hand-written tests for UI states            | open                           |

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
