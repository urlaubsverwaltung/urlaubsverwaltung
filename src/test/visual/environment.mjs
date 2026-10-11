import { readFileSync } from "node:fs";

/**
 * The instant the application clock is frozen at. Read from the 'visualtest' profile, so server and browser time
 * have a single source and cannot drift apart.
 */
export function readFixedInstant() {
  const profile = readFileSync(new URL("../../main/resources/application-visualtest.yaml", import.meta.url), "utf8");
  const match = profile.match(/^\s*fixed-instant:\s*(\S+)\s*$/m);
  if (!match) {
    throw new Error("could not read 'uv.development.clock.fixed-instant' from application-visualtest.yaml");
  }
  return match[1];
}
