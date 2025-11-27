const isProduction = process.env.NODE_ENV === "production";

module.exports = {
  plugins: [
    require("@tailwindcss/postcss"),
    require("postcss-url")({ url: "inline" }),
    isProduction && require("cssnano")({ preset: "default" }),
  ].filter(Boolean),
};
