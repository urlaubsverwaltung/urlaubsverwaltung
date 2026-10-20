import { readFileSync } from "node:fs";

/**
 * The instant the application clock is frozen at. Read from the 'visualtest' profile, so server and browser time
 * have a single source and cannot drift apart.
 */
export function readFixedInstant() {
  const profile = readFileSync(new URL("../../main/resources/application-visualtest.yaml", import.meta.url), "utf8");
  // optionally quoted, optionally followed by a comment
  const match = profile.match(/^\s*fixed-instant:\s*(["']?)([^"'\s#]+)\1\s*(#.*)?$/m);
  if (!match) {
    throw new Error("could not read 'uv.development.clock.fixed-instant' from application-visualtest.yaml");
  }
  const fixedInstant = match[2];
  if (Number.isNaN(Date.parse(fixedInstant))) {
    throw new TypeError(`'uv.development.clock.fixed-instant' is no valid instant: ${fixedInstant}`);
  }
  return fixedInstant;
}
