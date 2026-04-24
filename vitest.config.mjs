import { defineConfig } from 'vitest/config';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

export default defineConfig({
  test: {
    globals: true,
    environment: "jsdom",
    environmentOptions: {
      jsdom: {
        url: "http://localhost",
      },
    },
    include: ["src/main/javascript/**/*.spec.js"],
    alias: {
      "\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga)$": path.resolve(
        __dirname,
        "src/test/javascript/__mocks__/fileMock.js",
      ),
      "\\.(css|less)$": path.resolve(__dirname, "src/test/javascript/__mocks__/styleMock.js"),
    },
    coverage: {
      provider: "v8",
      reportsDirectory: "target/js-coverage",
      include: ["src/main/javascript/**/*.js"],
      exclude: ["**/*.{test,spec}.js", "**/__tests__/**"],
    },
  },
});
