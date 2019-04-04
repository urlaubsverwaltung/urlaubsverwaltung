module.exports = {
  rules: {
    'no-date-fns': require('./rules/no-date-fns'),
  },
  configs: {
    recommended: {
      // eslint-disable-next-line unicorn/prevent-abbreviations
      env: {
        es6: true
      },
      plugins: [
        'urlaubsverwaltung'
      ],
      rules: {
        'urlaubsverwaltung/no-date-fns': 'error',
      },
    },
  },
};
