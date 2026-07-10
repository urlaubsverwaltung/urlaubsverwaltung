import globals from "globals";
import pluginJs from "@eslint/js";
import eslintPluginUnicorn from "eslint-plugin-unicorn";

export default [
  pluginJs.configs.recommended,
  eslintPluginUnicorn.configs["flat/recommended"],
  {
    languageOptions: {
      globals: {
        ...globals.browser,
        Urlaubsverwaltung: true,
      },
    },
    rules: {
      "no-restricted-globals": [
        "error",
        {
          name: "fetch",
          message: "Use 'js/fetch' instead.",
        },
      ],
      "no-restricted-imports": [
        "error",
        {
          name: "date-fns",
          importNames: ["format", "parse", "start-of-week"],
          message: "Please use our custom functions from 'lib/date-fns' instead.",
        },
        {
          name: "fetch",
          importNames: ["format", "parse", "start-of-week"],
          message: "Please use our custom functions from 'lib/date-fns' instead.",
        },
      ],

      /* would require rewriting object-literal components (calendar, nav-tabs, ...) as ES6 classes */
      "unicorn/no-this-outside-of-class": "off",
      /* conflicts with our established module-level singleton state pattern (tooltip, theme-picker, ...) */
      "unicorn/no-top-level-assignment-in-function": "off",
      /* we intentionally expose bundle entry points on globalThis and mock globals in tests */
      "unicorn/no-global-object-property-assignment": "off",
      /* __tests__ is our established test directory convention */
      "unicorn/filename-case": "off",
      /* default replacement list clashes with our domain vocabulary (e.g. "application" -> "app") */
      "unicorn/name-replacements": "off",
    },
  },
  {
    files: ["**/*.spec.js", "**/*.test.js"],
    languageOptions: {
      globals: globals.vitest,
    },
    rules: {
      "no-restricted-imports": "off",
      "unicorn/consistent-function-scoping": "off",
    },
  },
  {
    files: [
      "rollup.config.mjs",
      "rollup-plugin-assets-manifest.mjs",
      "eslint.config.mjs",
      "postcss.config.js",
      "vitest.config.mjs",
    ],
    languageOptions: {
      globals: globals.node,
    },
  },
];
