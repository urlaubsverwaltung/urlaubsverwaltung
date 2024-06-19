/** @type {import('tailwindcss').Config} */
const plugin = require("tailwindcss/plugin");
const defaultConfig = require("tailwindcss/defaultConfig.js");

function withOpacity(variable) {
  return ({ opacityValue }) => (opacityValue ? `rgba(var(${variable}), ${opacityValue})` : `rgb(var(${variable}))`);
}

module.exports = {
  content: [
    "./src/main/javascript/**/*.js",
    "./src/main/resources/templates/**/*.html",
    "./src/main/resources/templates/**/*.svg",
  ],
  // use a prefix to not conflict with bootstrap
  prefix: "tw-",
  darkMode: "class",
  // use important keyword for tailwind utility classes to override bootstrap selectors
  important: true,
  corePlugins: {
    // disable tailwind base/reset styles. we're still using bootstrap
    preflight: false,
  },
  theme: {
    extend: {
      fontFamily: {
        logo: ["KaushanScript"],
      },
      borderWidth: {
        3: "3px",
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
      colors: {
        "black-almost": "#444444",
        "bootstrap-green": "#5cb85c",
        "bootstrap-green-dark": "#449d44",
      },
      borderColor: {
        calendar: {
          DEFAULT: withOpacity("--uv-cal-border-color"),
          today: withOpacity("--uv-cal-today-border-color"),
        },
      },
      textColor: {
        calendar: {
          DEFAULT: withOpacity("--uv-cal-default-color"),
          past: withOpacity("--uv-cal-past-color"),
          weekend: withOpacity("--uv-cal-weekend-color"),
          "public-holiday": withOpacity("--uv-cal-public-holiday-color"),
        },
      },
      backgroundColor: {
        calendar: {
          DEFAULT: withOpacity("--uv-cal-default-bg"),
          past: withOpacity("--uv-cal-past-bg"),
          weekend: withOpacity("--uv-cal-weekend-bg"),
          // ----
          "public-holiday": withOpacity("--uv-cal-public-holiday-bg"),
        },
      },
    },
    screens: {
      // cannot use 'extend' as `xs` would override other screens
      // since it's added to the bottom of the css file
      xs: "480px",
      ...defaultConfig.theme.screens,
    },
  },
  plugins: [
    plugin(function ({ addVariant }) {
      addVariant(
        "supports-backdrop-blur",
        "@supports (backdrop-filter: blur(0)) or (-webkit-backdrop-filter: blur(0))",
      );
    }),
  ],
};
