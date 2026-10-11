// Starts the Playwright browser server in the pinned Linux container.
//
// Screenshots must be rendered on the same platform for everyone (macOS and Linux render fonts differently), so the
// browser always runs in this container while the test runner runs natively and connects remotely.
// The image tag is derived from the @playwright/test version in package.json, so client and server stay in lockstep.
import { spawn } from "node:child_process";
import { readFileSync } from "node:fs";

const packageJson = JSON.parse(readFileSync(new URL("../../../package.json", import.meta.url), "utf8"));
const version = packageJson.devDependencies["@playwright/test"];
const port = process.env.PLAYWRIGHT_SERVER_PORT ?? "3000";

const args = [
  "run",
  "--rm",
  "--init",
  "--name",
  "urlaubsverwaltung-playwright",
  "--publish",
  `127.0.0.1:${port}:${port}`,
  "--workdir",
  "/home/pwuser",
  "--user",
  "pwuser",
  `mcr.microsoft.com/playwright:v${version}-noble`,
  "/bin/sh",
  "-c",
  `npx -y playwright@${version} run-server --port ${port} --host 0.0.0.0`,
];

console.log(`starting playwright ${version} browser server on port ${port}`);
const docker = spawn("docker", args, { stdio: "inherit" });
docker.on("exit", (code) => process.exit(code ?? 0));
for (const signal of ["SIGINT", "SIGTERM"]) {
  process.on(signal, () => docker.kill(signal));
}
