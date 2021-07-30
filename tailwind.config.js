const defaultConfig = require("tailwindcss/defaultConfig.js");
const plugin = require("tailwindcss/plugin");

module.exports = {
  purge: {
    content: [
      "./src/main/webapp/**/*.jsp",
      "./src/main/webapp/**/*.tag",
      "./src/main/javascript/**/*.js",
      "./src/main/resources/templates/**/*.html",
    ],
  },
  // use a prefix to not conflict with bootstrap
  prefix: "tw-",
  // use important keyword for tailwind utility classes to override bootstrap selectors
  important: true,
  corePlugins: {
    // disable tailwind base/reset styles. we're still using bootstrap
    preflight: false,
  },
  theme: {
    extend: {
      screens: {
        print: { raw: "print" },
      },
      lineHeight: {
        normal: "normal",
      },
      fontSize: {
        "10rem": "10rem",
      },
      margin: {
        25: "6.25rem",
      },
    },
    screens: {
      // cannot use 'extend' as `xs` would override other screens
      // since it's added to the bottom of the css file
      xs: "480px",
      ...defaultConfig.theme.screens,
    },
  },
  variants: {
    boxShadow: ["responsive", "hover", "focus", "focus-within"],
  },
  plugins: [
    plugin(function ({ addUtilities }) {
      const printStyles = {
        ".no-break-inside": {
          "break-inside": "avoid",
        },
        ".no-page-break-inside": {
          "break-inside": "avoid-page",
        },
        ".no-col-break-inside": {
          "break-inside": "avoid-column",
        },
      };
      addUtilities(printStyles, ["responsive"]);
    }),
  ],
};
