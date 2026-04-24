import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    globals: true,
    environment: "jsdom",
    environmentOptions: {
      jsdom: {
        url: "http://localhost",
      },
    },
    include: ['src/main/javascript/**/*.spec.js'],
    coverage: {
      provider: "v8",
      reportsDirectory: "target/js-coverage",
      include: ["src/main/javascript/**/*.js"],
      exclude: ["**/*.{test,spec}.js", "**/__tests__/**"],
    },
  },
});
