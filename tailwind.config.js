const defaultConfig = require("tailwindcss/defaultConfig.js");

function withOpacity(variable) {
  return ({ opacityValue }) => (opacityValue ? `rgba(var(${variable}), ${opacityValue})` : `rgb(var(${variable}))`);
}

module.exports = {
  content: [
    "./src/main/webapp/**/*.jsp",
    "./src/main/webapp/**/*.tag",
    "./src/main/javascript/**/*.js",
    "./src/main/resources/templates/**/*.html",
  ],
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
          hover: withOpacity("--uv-cal-default-color-hover"),
          past: withOpacity("--uv-cal-past-color"),
          "past-hover": withOpacity("--uv-cal-past-color-hover"),
          weekend: withOpacity("--uv-cal-weekend-color"),
          "weekend-hover": withOpacity("--uv-cal-weekend-color-hover"),
        },
        "sick-note": withOpacity("--uv-cal-sick-note-color"),
        "sick-note-hover": withOpacity("--uv-cal-sick-note-color-hover"),
        "public-holiday": withOpacity("--uv-cal-public-holiday-color"),
        "public-holiday-hover": withOpacity("--uv-cal-public-holiday-color-hover"),
        "personal-holiday": withOpacity("--uv-cal-personal-holiday-color"),
        "personal-holiday-hover": withOpacity("--uv-cal-personal-holiday-color-hover"),
        "personal-holiday-approved": withOpacity("--uv-cal-personal-holiday-approved-color"),
        "personal-holiday-approved-hover": withOpacity("--uv-cal-personal-holiday-approved-color-hover"),
      },
      backgroundColor: {
        calendar: {
          DEFAULT: withOpacity("--uv-cal-default-bg"),
          hover: withOpacity("--uv-cal-default-bg-hover"),
          past: withOpacity("--uv-cal-past-bg"),
          "past-hover": withOpacity("--uv-cal-past-hover-bg"),
          weekend: withOpacity("--uv-cal-weekend-bg"),
          "weekend-hover": withOpacity("--uv-cal-weekend-bg-hover"),
        },
        "sick-note": withOpacity("--uv-cal-sick-note-bg"),
        "sick-note-hover": withOpacity("--uv-cal-sick-note-bg-hover"),
        "public-holiday": withOpacity("--uv-cal-public-holiday-bg"),
        "public-holiday-hover": withOpacity("--uv-cal-public-holiday-bg-hover"),
        "personal-holiday": withOpacity("--uv-cal-personal-holiday-bg"),
        "personal-holiday-hover": withOpacity("--uv-cal-personal-holiday-bg-hover"),
        "personal-holiday-approved": withOpacity("--uv-cal-personal-holiday-approved-bg"),
        "personal-holiday-approved-hover": withOpacity("--uv-cal-personal-holiday-approved-bg-hover"),
      },
    },
    screens: {
      // cannot use 'extend' as `xs` would override other screens
      // since it's added to the bottom of the css file
      xs: "480px",
      ...defaultConfig.theme.screens,
    },
  },
  plugins: [],
};
