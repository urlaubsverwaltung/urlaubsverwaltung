import globals from "globals";
import pluginJs from "@eslint/js";
import eslintPluginUnicorn from 'eslint-plugin-unicorn';


export default [
  pluginJs.configs.recommended,
  eslintPluginUnicorn.configs['flat/recommended'],
  {
    languageOptions: {
      globals: {
        ...globals.browser,
        "Urlaubsverwaltung": true,
      },
    },
    rules: {
      "no-restricted-globals": [
        "error",
        {
          "name": "fetch",
          "message": "Use 'js/fetch' instead."
        },
      ],
      "no-restricted-imports": ["error",
        {
          "name": "date-fns",
          "importNames": ["format", "parse", "start-of-week"],
          "message": "Please use our custom functions from 'lib/date-fns' instead."
        },
        {
          "name": "fetch",
          "importNames": ["format", "parse", "start-of-week"],
          "message": "Please use our custom functions from 'lib/date-fns' instead."
        },
      ],

      "unicorn/no-fn-reference-in-iterator": "off",
      "unicorn/prefer-reflect-apply": "off",
      /* our webpack build cannot handle top-level-await atm */
      "unicorn/prefer-top-level-await": "off",
    },
  },
  {
    files: ["**/*.spec.js", "**/*.test.js"],
    languageOptions: {
      globals: globals.jest,
    },
    rules: {
      "no-restricted-imports": "off",
      "unicorn/consistent-function-scoping": "off",
    }
  },
  {
    files: ["**/*.config.m?js"],
    languageOptions: {
      globals: globals.node,
    }
  },
];
