import { expect, test } from "./fixtures.mjs";
import { loadManifest, manifestFile, saveManifest, toPattern } from "./manifest.mjs";
import { authFile, roles } from "./roles.mjs";

// Crawls the application per role and compares the discovered pages with the committed manifest - just like
// snapshot tests: new or removed pages fail the test, '-u' updates the manifest. Orphaned screenshots are handled by
// screenshots.teardown.mjs, after all screenshot tests have finished.
//
// | updateSnapshots | manifest                                                |
// |-----------------|---------------------------------------------------------|
// | none            | new or removed pages -> fail                            |
// | missing         | add new pages, new or removed pages -> fail             |
// | changed (-u)    | add new pages, remove stale pages                       |
// | all             | rebuild the manifest, keep 'ignored' decisions          |

const MAX_DEPTH = 5;

const EXCLUDED = [
  // not part of the web ui
  /^\/api(\/|$)/,
  /^\/actuator(\/|$)/,
  /^\/logout$/,
  // downloads: csv exports and ical feeds
  /\/download$/,
  /\/export$/,
  /^\/web\/(company\/|departments\/\d+\/)?persons\/\d+\/calendar$/,
  // redirects to google
  /^\/web\/google-api-handshake$/,
];

function isCrawlable(url, origin) {
  return (
    url.origin === origin && url.pathname.startsWith("/web") && !EXCLUDED.some((regex) => regex.test(url.pathname))
  );
}

async function crawl(page, origin) {
  const discovered = new Map();
  const start = "/web/overview";
  const queuedUrls = new Set([start]);
  const queue = [{ url: start, depth: 0 }];

  while (queue.length > 0) {
    const { url, depth } = queue.shift();

    // a pattern counts as discovered only once one of its urls loaded successfully. until then further urls of the
    // same pattern stay in the queue, so a single failing url does not drop the page from the manifest.
    if (discovered.has(toPattern(url))) {
      continue;
    }

    let response;
    try {
      response = await page.goto(url);
    } catch (error) {
      console.warn(`skipping ${url}: ${error.message.split("\n")[0]}`);
      continue;
    }

    const contentType = response?.headers()["content-type"] ?? "";
    if (!response?.ok() || !contentType.includes("text/html")) {
      console.warn(`skipping ${url}: status ${response?.status()}, content type '${contentType}'`);
      continue;
    }

    // redirects are recorded with their target, e.g. '/web/overview' is the overview of the logged in person
    const finalUrl = new URL(page.url());
    const finalPattern = toPattern(finalUrl.pathname);
    if (discovered.has(finalPattern)) {
      continue;
    }
    discovered.set(finalPattern, finalUrl.pathname);

    if (depth >= MAX_DEPTH) {
      continue;
    }

    await page.waitForLoadState("networkidle");
    const hrefs = await page.$$eval("a[href]", (anchors) => anchors.map((anchor) => anchor.href));
    const links = [...new Set(hrefs)]
      .map((href) => new URL(href))
      .filter((link) => isCrawlable(link, origin))
      // query parameters only filter data, the fragment does not address another page
      .map((link) => link.pathname)
      .toSorted();

    for (const link of links) {
      if (!queuedUrls.has(link) && !discovered.has(toPattern(link))) {
        queuedUrls.add(link);
        queue.push({ url: link, depth: depth + 1 });
      }
    }
  }

  return discovered;
}

function updateManifest(manifest, discovered, mode) {
  if (mode === "all") {
    return Object.fromEntries(
      [...discovered].map(([pattern, url]) => {
        const previous = manifest[pattern];
        return [pattern, previous?.status === "ignored" ? previous : { url, status: "covered" }];
      }),
    );
  }

  const updated = { ...manifest };
  for (const [pattern, url] of discovered) {
    updated[pattern] ??= { url, status: "covered" };
  }
  if (mode === "changed") {
    for (const pattern of Object.keys(updated)) {
      if (!discovered.has(pattern)) {
        delete updated[pattern];
      }
    }
  }
  return updated;
}

for (const role of roles) {
  test.describe(role.name, () => {
    test.use({ storageState: authFile(role) });

    test(`discover pages of ${role.name}`, async ({ page, baseURL }) => {
      test.setTimeout(15 * 60 * 1000);

      const mode = test.info().config.updateSnapshots;
      const manifest = loadManifest(role);
      const discovered = await crawl(page, new URL(baseURL).origin);

      const newPatterns = [...discovered.keys()].filter((pattern) => !(pattern in manifest));
      const removedPatterns = Object.keys(manifest).filter((pattern) => !discovered.has(pattern));

      if (mode !== "none") {
        const updated = updateManifest(manifest, discovered, mode);
        if (JSON.stringify(updated) !== JSON.stringify(manifest)) {
          saveManifest(role, updated);
        }
      }

      const updatesAccepted = mode === "changed" || mode === "all";
      const problems = updatesAccepted
        ? []
        : [
            ...newPatterns.map((pattern) => `new page: ${pattern}`),
            ...removedPatterns.map((pattern) => `removed page: ${pattern}`),
          ];

      const hint = mode === "missing" && newPatterns.length > 0 ? " (new pages have been added)" : "";
      expect(problems, `pages of ${role.name} differ from ${manifestFile(role)}${hint}, run with -u to accept`).toEqual(
        [],
      );
    });
  });
}
