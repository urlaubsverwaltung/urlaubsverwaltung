const isProduction = process.env.NODE_ENV === "production";

module.exports = {
  plugins: [
    require("postcss-import"),
    require("postcss-nested"),
    require("tailwindcss"),
    require("autoprefixer"),
    require("postcss-url")({ url: "inline" }),
    isProduction && require("cssnano")({ preset: "default" }),
  ].filter(Boolean),
};
