const isProduction = process.env.NODE_ENV === "production";

module.exports = {
  plugins: [
    require("@tailwindcss/poatcss"),
    isProduction && require("cssnano")({ preset: "default" }),
  ].filter(Boolean),
};
