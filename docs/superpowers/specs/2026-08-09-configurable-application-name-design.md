# Configurable application name

## Problem

The name "Urlaubsverwaltung" is baked into the UI and into several outputs the
application produces. Operators who run their own instance cannot rebrand it.
The most visible occurrence is the wordmark in the top left corner of every
page (`templates/_layout.html`), but the name also reaches users through page
titles, outgoing mail, exported calendar files, the API documentation and the
PWA manifest.

## Goal

A single application property sets the name shown to users everywhere it
appears. The default keeps today's behaviour, so existing installations see no
change.

## Non-goals

- No runtime editing through the settings UI. The name is deployment
  configuration, not a tenant setting.
- No renaming of technical identifiers: `spring.application.name`, the OIDC
  role prefix `urlaubsverwaltung_`, the database name, and the Google Calendar
  API client name stay as they are. They identify the software, not the brand.
- No change to prose in message bundles that happens to mention
  "Urlaubsverwaltung" (role descriptions, settings help texts). Rewriting
  translated sentences to interpolate a name is a separate, larger job.

## The property

```java
@Validated
@ConfigurationProperties("uv.branding")
public record BrandingProperties(@NotEmpty String name) { }
```

`uv.branding.name` defaults to `Urlaubsverwaltung` in `application.yaml` and is
documented in `README.md`.

`uv.application` is already taken by the vacation-application domain
(`application/ApplicationProperties.java`), so `uv.application.name` would be
misleading. Hence `uv.branding`.

## Distribution

`BrandingDataProvider implements DataProviderInterface` adds an
`applicationName` model attribute to every rendered view. This follows the
existing pattern of `LocaleModelInterceptor` and `UserThemeDataProvider`. It is
deliberately separate from `FrameDataProvider`, which requires a signed-in user
and therefore cannot serve anonymous or error views.

The non-web consumers (mail, iCal, Swagger, manifest) inject
`BrandingProperties` directly.

## Consumers

### 1. Wordmark

`templates/_layout.html` renders the link text with `th:text="${applicationName}"`,
keeping the literal as Thymeleaf prototype text.

### 2. Page titles

Every page title gains a ` – <name>` suffix.

`_layout.html`'s head fragment does `<title th:replace="${title}">`, which
substitutes the page's whole `<title>` element. Thymeleaf cannot append text to
a replaced element, so this cannot be solved in the layout alone.

A shared fragment holds the format:

```html
<title th:fragment="pageTitle(text)" th:text="|${text} – ${applicationName}|"></title>
```

Each page that uses `_layout::head` changes its title element to
`<title th:replace="~{fragments/page-title::pageTitle(#{key})}"></title>`.
The separator lives in exactly one file.

`calendar.share.header.title` loses its hardcoded prefix in all bundles and
becomes just "Kalenderfreigabe" / "Calendar sharing", because the suffix now
comes from the fragment.

### 3. Mail

`uv.mail.fromDisplayName` and `uv.mail.replyToDisplayName` lose their hardcoded
defaults in `application.yaml` and fall back to the branding name when unset.
An explicitly configured value still wins, so existing configurations are
unaffected.

### 4. iCal and Swagger

`ICalService` builds its `ProdId` as `-//<name>//iCal4j 1.0//DE`.
`SwaggerConfig` uses the name for the API title and the contact name.

`GoogleCalendarClientProvider.APPLICATION_NAME` stays hardcoded: it is an API
client identifier sent to Google, not user-visible text.

### 5. PWA manifest

The static `static/site.webmanifest` is replaced by a controller rendering
`application/manifest+json` from a template, with `name` and `short_name` taken
from the branding property. This also fixes today's values, which read
`urlaubsverwaltung.cloud` / `uv.cloud` and point `start_url` at the hosted
instance regardless of where the application actually runs.

The path `/site.webmanifest` and its `permitAll()` rule in
`SecurityWebConfiguration` are unchanged, so `_layout.html` needs no edit.

## Cleanup

The message keys `header.title` and `nav.urlaubsverwaltung.title` are removed.
No template references either, and `nav.urlaubsverwaltung.title` is already
empty in `messages_en`.

## Testing

- `BrandingDataProviderTest` — the model attribute is added, and skipped for
  redirect/forward views.
- `MailPropertiesTest` — display names fall back to the branding name when
  unset and keep an explicit value when set.
- `ICalServiceTest` — the exported calendar carries the configured `ProdId`.
- `WebManifestViewControllerTest` — the rendered manifest carries the
  configured name and the correct content type.
- A Thymeleaf rendering test asserting that wordmark and page title reflect a
  custom configured name.
