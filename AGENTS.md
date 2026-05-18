# AGENTS Guide for `urlaubsverwaltung`

## Scope

This guide is for agents working on feature development, bug fixes, and writing tests in the urlaubsverwaltung codebase. Before starting any task, read the relevant section below. After every change, run `./mvnw clean verify` to confirm nothing is broken.

## Big Picture
- Spring Boot monolith (Java 25) with **package-by-feature** structure under `src/main/java/org/synyx/urlaubsverwaltung/*` (e.g. `application/`, `sicknote/`, `person/`).
- UI is server-rendered Thymeleaf (`@Controller`, `/web/**`) plus REST APIs (`@RestController`, `/api/**`), both sharing the same domain services.
- Typical domain flow is **orchestration in interaction services**: persist entity, write comment/history, send mails, publish domain events (see `application/application/ApplicationInteractionServiceImpl.java`).
- Persistence is JPA + PostgreSQL; schema is managed via Liquibase master changelog `src/main/resources/dbchangelogs/changelogmaster.xml`.
- Tenancy is foundational: all entities are tenant-aware via `AbstractTenantAwareEntity` + `TenantListener`.

## Hard Rules Enforced by Tests
- Every JPA `@Entity` must extend `AbstractTenantAwareEntity` (`src/test/java/org/synyx/urlaubsverwaltung/EntityArchTest.java`).
- Components that implement `SchedulingConfigurer` or listen to `ApplicationStartedEvent`/`ApplicationReadyEvent` must be annotated with `@ConditionalOnSingleTenantMode` (`src/test/java/org/synyx/urlaubsverwaltung/SingleTenantArchTest.java`).

## Build and Test Workflows
- Full CI-like build from repo root: `./mvnw clean verify`.
- Maven build includes frontend steps via `frontend-maven-plugin`: `npm ci`, `npm run lint`, `npm run build`, and `npm run test:coverage` (see `pom.xml`).
- Local app with demo data: `./mvnw clean spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=demodata"`.
- Local infra defaults come from `docker-compose.yml` (Postgres `5434`, Mailpit `1025/8025`, Keycloak `8090`).
- UI test profile only: `./mvnw verify -Pui-test` (skips surefire + JS lint/tests, runs failsafe group `ui`).

## Frontend/Asset Pipeline Conventions
- Frontend sources: `src/main/javascript` (`bundles`, `components`, `js`, `lib`) and CSS bundles in `src/main/css/bundles`.
- Rollup/PostCSS emit hashed assets + `assets-manifest.json`; Thymeleaf resolves logical names through `AssetManifestService` and `web/thymeleaf/AssetAttributeTagProcessor.java`.
- If asset resolution fails, run `npm run build` (manifest file is required at runtime).
- ESLint forbids direct global `fetch` and some direct `date-fns` imports; use project wrappers from `src/main/javascript/js/fetch` and `src/main/javascript/lib/date-fns` (`eslint.config.mjs`).

## Security and Integration Points
- Authentication is OIDC/OAuth2 client + resource server JWT; role-based URL rules are centralized in `security/SecurityWebConfiguration.java`.
- First successful login can bootstrap Office role (documented in `README.md`), so auth config matters during first startup.
- Calendar integrations and Google handshake flow live in `calendarintegration/`.
- Optional extension mode is controlled by `uv.extensions.enabled` (`extension/ExtensionConfiguration.java`).
- Deployment artifacts include Docker image setup in `pom.xml` and Helm chart in `charts/urlaubsverwaltung/`.

## Test Conventions

- **Unit tests** use `@ExtendWith(MockitoExtension.class)` — no Spring context, MockMvc via `standaloneSetup()`.
  - Naming: `*ViewControllerTest.java` (controller), `*ServiceImplTest.java` (service).
  - Variable for class under test: `sut`. Setup in `@BeforeEach setUp()` via constructor injection.
  - Assertions: AssertJ (`assertThat`). Test method prefix: `ensure*`.
- **Integration tests** use `@SpringBootTest` + `extends SingleTenantTestContainersBase` (spins up a real PostgreSQL container).
  - Naming suffix: `*IT.java` (general) or `*RepositoryIT.java` (persistence layer).
  - Annotate the test class with `@Transactional`.
- **UI/playwright tests** use `@SpringBootTest(webEnvironment = RANDOM_PORT)`.
  - Naming suffix: `*UIIt.java`.
- Mirror source structure: `src/main/java/.../application/` → `src/test/java/.../application/`.

## Liquibase Workflow

- Changelogs live in `src/main/resources/dbchangelogs/`.
- Naming: `changelog-{VERSION}-{kebab-description}.xml` — version matches the release (e.g. `changelog-5.35.0-add-spring-session.xml`).
- Each `<changeSet>` needs a unique `id` (use `--` for sequential steps, e.g. `add-department-membership--migrate-members`) and an `author`.
- Use `<preConditions>` when a column/table might already exist.
- After creating the file, add an `<include relativeToChangelogFile="true" file="changelog-…xml"/>` line at the **end** of `changelogmaster.xml`.

## Interaction Service Pattern

When adding a lifecycle operation to a domain entity, follow this sequence in the InteractionService method:
1. **Persist** — save entity state via the domain service
2. **Comment** — write a history/audit comment
3. **Mail** — send relevant notifications
4. **Event** — publish a domain event via `ApplicationEventPublisher`

Create a **new** InteractionService when a domain entity gains independent lifecycle operations (like `ApplicationInteractionService`, `SickNoteInteractionService`). Add methods to an **existing** service when extending the same entity's lifecycle. Sub-workflows that are tightly scoped get their own service and delegate to the parent (e.g. `SickNoteExtensionInteractionServiceImpl`).

Annotate classes with `@Service @Transactional` and use constructor injection only — never field injection.

## Commit and PR Conventions

- Commit messages follow [tbaggery style](http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html): lowercase subject, imperative mood, ≤72 chars, no trailing period.
- Reference issues in the body: `closes #XXXX`.
- PRs should rebase against `main` before merge.
- Security issues go through GitHub Security Advisories — not public issues.

## Off-Limits: Do Not Touch Without Explicit Instruction

- **`security/SecurityWebConfiguration.java`** — URL authorization rules affect all roles; wrong changes silently open or block access.
- **`AbstractTenantAwareEntity` and `TenantListener`** — tenancy is foundational infrastructure; changes break data isolation across all tenants.
- **Existing Liquibase changesets** — once a changeset is deployed it must never be modified; add a new one instead.
- **ArchTests** (`EntityArchTest.java`, `SingleTenantArchTest.java`) — these encode hard invariants; do not weaken or delete them.

## Useful Files to Read First
- `README.md` (runtime config, demodata profile, frontend/test commands)
- `pom.xml` + `package.json` (actual build/test execution path)
- `src/main/resources/application.yaml` + `src/main/resources/application-demodata.yaml`
- `src/main/java/org/synyx/urlaubsverwaltung/application/application/ApplicationInteractionServiceImpl.java`
- `src/main/java/org/synyx/urlaubsverwaltung/security/SecurityWebConfiguration.java`

