const defaultConfig = require("tailwindcss/defaultConfig.js");
const plugin = require("tailwindcss/plugin");

module.exports = {
  purge: {
    content: ["./src/main/webapp/**/*.jsp", "./src/main/webapp/**/*.tag", "./src/main/javascript/**/*.js"],
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
    borderWidth: {
      DEFAULT: "1px",
      0: "0",
      2: "2px",
      3: "3px",
      4: "4px",
    },
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
