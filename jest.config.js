// For a detailed explanation regarding each configuration property, visit:
// https://jestjs.io/docs/en/configuration.html

module.exports = {
  roots: [
    "<rootDir>/src/main/javascript"
  ],
  collectCoverage: false,
  collectCoverageFrom: [
    "**/*.js",
    "!**/*.{test,spec}.js",
    "!**/__tests__/**",
    "!**/WEB-INF/**"
  ],
  coverageDirectory: "<rootDir>/target/js-coverage",
  testURL: "http://localhost",
  testEnvironment: "jsdom",
  moduleNameMapper: {
    "\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga)$": "<rootDir>/src/test/javascript/__mocks__/fileMock.js",
    "\\.(css|less)$": "<rootDir>/src/test/javascript/__mocks__/styleMock.js"
  }
};
