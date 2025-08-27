const isProduction = process.env.NODE_ENV === "production";

module.exports = {
  plugins: [
    require("@tailwindcss/postcss"),
    isProduction && require("cssnano")({ preset: "default" }),
  ].filter(Boolean),
};
