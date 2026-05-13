# AGENTS Guide for `urlaubsverwaltung`

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

## Useful Files to Read First
- `README.md` (runtime config, demodata profile, frontend/test commands)
- `pom.xml` + `package.json` (actual build/test execution path)
- `src/main/resources/application.yaml` + `src/main/resources/application-demodata.yaml`
- `src/main/java/org/synyx/urlaubsverwaltung/application/application/ApplicationInteractionServiceImpl.java`
- `src/main/java/org/synyx/urlaubsverwaltung/security/SecurityWebConfiguration.java`

