import { rmSync } from "node:fs";
import { expect, test } from "./fixtures.mjs";
import { existingScreenshots, loadManifest, screenshotDirectory, screenshotName } from "./manifest.mjs";
import { roles } from "./roles.mjs";

// Compares the baseline screenshots with the manifests. Runs as teardown of the setup project, which is after discovery
// and all screenshot tests have finished: during the run a screenshot test of a page that discovery just removed from
// the manifest could still write its baseline again.
//
// | updateSnapshots | orphaned screenshots |
// |-----------------|----------------------|
// | none, missing   | fail                 |
// | changed (-u)    | delete               |
// | all             | delete               |

for (const role of roles) {
  test(`screenshots of ${role.name} match the manifest`, () => {
    const mode = test.info().config.updateSnapshots;
    const updatesAccepted = mode === "changed" || mode === "all";

    const patternsByScreenshot = Map.groupBy(
      Object.entries(loadManifest(role))
        .filter(([, entry]) => entry.status === "covered")
        .map(([pattern]) => pattern),
      (pattern) => screenshotName(role, pattern).join("/"),
    );
    const collisions = [...patternsByScreenshot.values()].filter((patterns) => patterns.length > 1);

    const orphanedScreenshots = existingScreenshots(role)
      .filter(({ name }) => !patternsByScreenshot.has(name.join("/")))
      .map(({ project, name }) => `${project}/${name.join("/")}`);
    if (updatesAccepted) {
      for (const screenshot of orphanedScreenshots) {
        rmSync(`${screenshotDirectory}/${screenshot}`);
      }
    }

    const problems = [
      ...(updatesAccepted ? [] : orphanedScreenshots.map((screenshot) => `orphaned screenshot: ${screenshot}`)),
      // never accepted, two pages would share one screenshot
      ...collisions.map((patterns) => `pages with the same screenshot name: ${patterns.join(", ")}`),
    ];
    expect(problems, `screenshots of ${role.name} differ from its manifest, run with -u to accept`).toEqual([]);
  });
}
