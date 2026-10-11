import { existsSync, mkdirSync, readFileSync, readdirSync, writeFileSync } from "node:fs";
import { dirname } from "node:path";
import { fileURLToPath } from "node:url";

// The manifest is the golden master of the pages to verify, one file per role:
// { "<url pattern>": { "url": "<example url>", "status": "covered" | "ignored", "reason": "<why ignored>" } }

const visualTestDirectory = dirname(fileURLToPath(import.meta.url));
export const screenshotDirectory = `${visualTestDirectory}/__screenshots__`;

export function manifestFile(role) {
  return `${visualTestDirectory}/pages/${role.name}.json`;
}

export function loadManifest(role) {
  const file = manifestFile(role);
  return existsSync(file) ? JSON.parse(readFileSync(file, "utf8")) : {};
}

export function saveManifest(role, manifest) {
  const file = manifestFile(role);
  mkdirSync(dirname(file), { recursive: true });
  const sorted = Object.fromEntries(Object.entries(manifest).sort(([a], [b]) => a.localeCompare(b)));
  writeFileSync(file, JSON.stringify(sorted, undefined, 2) + "\n");
}

export function coveredPages(manifest) {
  return Object.entries(manifest)
    .filter(([, entry]) => entry.status === "covered")
    .map(([pattern, entry]) => ({ pattern, url: entry.url }));
}

/**
 * Ids in paths are collapsed, otherwise every person and every application would be a page of its own.
 * '/web/person/5/overview' -> '/web/person/{id}/overview'
 */
export function toPattern(pathname) {
  return pathname
    .split("/")
    .map((segment) => (/^\d+$/.test(segment) ? "{id}" : segment))
    .join("/");
}

/**
 * Name of the screenshot of the given pattern and role as path segments, relative to the project directory of the
 * screenshots. Playwright replaces everything but letters, digits and '-' in file names, so the slug consists of those
 * only. '/web/person/{id}/overview' -> ['office', 'web-person-id-overview.png']
 */
export function screenshotName(role, pattern) {
  const slug = pattern
    .replace(/^\//, "")
    .replaceAll(/[{}]/g, "")
    .replaceAll(/[^A-Za-z0-9-]+/g, "-");
  return [role.name, `${slug}.png`];
}

/**
 * All existing baseline screenshots of the given role, over all projects (desktop, mobile, ...), relative to the
 * screenshot directory.
 */
export function existingScreenshots(role) {
  if (!existsSync(screenshotDirectory)) {
    return [];
  }
  return readdirSync(screenshotDirectory, { withFileTypes: true })
    .filter((projectDirectory) => projectDirectory.isDirectory())
    .flatMap((projectDirectory) => {
      const roleDirectory = `${screenshotDirectory}/${projectDirectory.name}/${role.name}`;
      if (!existsSync(roleDirectory)) {
        return [];
      }
      return readdirSync(roleDirectory)
        .filter((file) => file.endsWith(".png"))
        .map((file) => ({ project: projectDirectory.name, name: [role.name, file] }));
    });
}
