// Fails when the last Playwright run failed. Used by the maven profile 'visual-test' in the verify phase: the test run
// itself must not fail the build, otherwise maven would skip stopping the application in post-integration-test.
//
// '--reset' deletes the result of a previous run before the tests start, so a run aborted before playwright wrote its
// result cannot pass with an outdated result.
import { existsSync, readFileSync, rmSync } from "node:fs";

const lastRunFile = new URL("../../../target/visual-tests/results/.last-run.json", import.meta.url);

if (process.argv.includes("--reset")) {
  rmSync(lastRunFile, { force: true });
} else if (existsSync(lastRunFile)) {
  const lastRun = JSON.parse(readFileSync(lastRunFile, "utf8"));
  if (lastRun.status !== "passed") {
    console.error(`visual regression tests ${lastRun.status}, see target/visual-tests/report`);
    process.exitCode = 1;
  }
} else {
  console.error("visual regression tests did not run, target/visual-tests/results/.last-run.json is missing");
  process.exitCode = 1;
}
