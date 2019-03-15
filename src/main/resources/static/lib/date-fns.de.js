(function () {
  // + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + +
  // +                                                                                                       +
  // +   monkey patch date-fn to support de locale without frontend build setup based on commonjs modules    +
  // +                                                                                                       +
  // + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + +

  if (typeof window.dateFns === 'undefined') {
    throw new Error("cannot monkeypatch date-fns as it is not loaded yet");
  }

  const originalFormat = dateFns.format;
  dateFns.format = function format(date, formatStr) {
    const options = {
      locale: {
        distanceInWords: buildDistanceInWords(),
        format: buildFormatLocale(),
      }
    };
    return originalFormat(date, formatStr, options);
  };

  //
  // copied from date-fn sources ٩( ᐛ )و
  //

  const buildDistanceInWords = (function() {
    function buildDistanceInWordsLocale () {
      var distanceInWordsLocale = {
        lessThanXSeconds: {
          standalone: {
            one: 'weniger als eine Sekunde',
            other: 'weniger als {{count}} Sekunden'
          },
          withPreposition: {
            one: 'weniger als einer Sekunde',
            other: 'weniger als {{count}} Sekunden'
          }
        },

        xSeconds: {
          standalone: {
            one: 'eine Sekunde',
            other: '{{count}} Sekunden'
          },
          withPreposition: {
            one: 'einer Sekunde',
            other: '{{count}} Sekunden'
          }
        },

        halfAMinute: {
          standalone: 'eine halbe Minute',
          withPreposition: 'einer halben Minute'
        },

        lessThanXMinutes: {
          standalone: {
            one: 'weniger als eine Minute',
            other: 'weniger als {{count}} Minuten'
          },
          withPreposition: {
            one: 'weniger als einer Minute',
            other: 'weniger als {{count}} Minuten'
          }
        },

        xMinutes: {
          standalone: {
            one: 'eine Minute',
            other: '{{count}} Minuten'
          },
          withPreposition: {
            one: 'einer Minute',
            other: '{{count}} Minuten'
          }
        },

        aboutXHours: {
          standalone: {
            one: 'etwa eine Stunde',
            other: 'etwa {{count}} Stunden'
          },
          withPreposition: {
            one: 'etwa einer Stunde',
            other: 'etwa {{count}} Stunden'
          }
        },

        xHours: {
          standalone: {
            one: 'eine Stunde',
            other: '{{count}} Stunden'
          },
          withPreposition: {
            one: 'einer Stunde',
            other: '{{count}} Stunden'
          }
        },

        xDays: {
          standalone: {
            one: 'ein Tag',
            other: '{{count}} Tage'
          },
          withPreposition: {
            one: 'einem Tag',
            other: '{{count}} Tagen'
          }

        },

        aboutXMonths: {
          standalone: {
            one: 'etwa ein Monat',
            other: 'etwa {{count}} Monate'
          },
          withPreposition: {
            one: 'etwa einem Monat',
            other: 'etwa {{count}} Monaten'
          }
        },

        xMonths: {
          standalone: {
            one: 'ein Monat',
            other: '{{count}} Monate'
          },
          withPreposition: {
            one: 'einem Monat',
            other: '{{count}} Monaten'
          }
        },

        aboutXYears: {
          standalone: {
            one: 'etwa ein Jahr',
            other: 'etwa {{count}} Jahre'
          },
          withPreposition: {
            one: 'etwa einem Jahr',
            other: 'etwa {{count}} Jahren'
          }
        },

        xYears: {
          standalone: {
            one: 'ein Jahr',
            other: '{{count}} Jahre'
          },
          withPreposition: {
            one: 'einem Jahr',
            other: '{{count}} Jahren'
          }
        },

        overXYears: {
          standalone: {
            one: 'mehr als ein Jahr',
            other: 'mehr als {{count}} Jahre'
          },
          withPreposition: {
            one: 'mehr als einem Jahr',
            other: 'mehr als {{count}} Jahren'
          }
        },

        almostXYears: {
          standalone: {
            one: 'fast ein Jahr',
            other: 'fast {{count}} Jahre'
          },
          withPreposition: {
            one: 'fast einem Jahr',
            other: 'fast {{count}} Jahren'
          }
        }
      }

      function localize (token, count, options) {
        options = options || {}

        var usageGroup = options.addSuffix
          ? distanceInWordsLocale[token].withPreposition
          : distanceInWordsLocale[token].standalone

        var result
        if (typeof usageGroup === 'string') {
          result = usageGroup
        } else if (count === 1) {
          result = usageGroup.one
        } else {
          result = usageGroup.other.replace('{{count}}', count)
        }

        if (options.addSuffix) {
          if (options.comparison > 0) {
            return 'in ' + result
          } else {
            return 'vor ' + result
          }
        }

        return result
      }

      return {
        localize: localize
      }
    }

    return buildDistanceInWordsLocale;
  })();


  const buildFormatLocale = (function() {
    var commonFormatterKeys = [
      'M', 'MM', 'Q', 'D', 'DD', 'DDD', 'DDDD', 'd',
      'E', 'W', 'WW', 'YY', 'YYYY', 'GG', 'GGGG',
      'H', 'HH', 'h', 'hh', 'm', 'mm',
      's', 'ss', 'S', 'SS', 'SSS',
      'Z', 'ZZ', 'X', 'x'
    ]

    function ordinal(number) {
      return number + '.'
    }

    function buildFormattingTokensRegExp(formatters) {
      var formatterKeys = []
      for (var key in formatters) {
        if (formatters.hasOwnProperty(key)) {
          formatterKeys.push(key)
        }
      }

      var formattingTokens = commonFormatterKeys
        .concat(formatterKeys)
        .sort()
        .reverse()
      var formattingTokensRegExp = new RegExp(
        '(\\[[^\\[]*\\])|(\\\\)?' + '(' + formattingTokens.join('|') + '|.)', 'g'
      )

      return formattingTokensRegExp
    }

    return function buildFormatLocale() {
      // Note: in German, the names of days of the week and months are capitalized.
      // If you are making a new locale based on this one, check if the same is true for the language you're working on.
      // Generally, formatted dates should look like they are in the middle of a sentence,
      // e.g. in Spanish language the weekdays and months should be in the lowercase.
      var months3char = ['Jan', 'Feb', 'Mär', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez']
      var monthsFull = ['Januar', 'Februar', 'März', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember']
      var weekdays2char = ['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa']
      var weekdays3char = ['Son', 'Mon', 'Die', 'Mit', 'Don', 'Fre', 'Sam']
      var weekdaysFull = ['Sonntag', 'Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag']
      var meridiemUppercase = ['AM', 'PM']
      var meridiemLowercase = ['am', 'pm']
      var meridiemFull = ['a.m.', 'p.m.']

      var formatters = {
        // Month: Jan, Feb, ..., Dec
        'MMM': function (date) {
          return months3char[date.getMonth()]
        },

        // Month: January, February, ..., December
        'MMMM': function (date) {
          return monthsFull[date.getMonth()]
        },

        // Day of week: Su, Mo, ..., Sa
        'dd': function (date) {
          return weekdays2char[date.getDay()]
        },

        // Day of week: Sun, Mon, ..., Sat
        'ddd': function (date) {
          return weekdays3char[date.getDay()]
        },

        // Day of week: Sunday, Monday, ..., Saturday
        'dddd': function (date) {
          return weekdaysFull[date.getDay()]
        },

        // AM, PM
        'A': function (date) {
          return (date.getHours() / 12) >= 1 ? meridiemUppercase[1] : meridiemUppercase[0]
        },

        // am, pm
        'a': function (date) {
          return (date.getHours() / 12) >= 1 ? meridiemLowercase[1] : meridiemLowercase[0]
        },

        // a.m., p.m.
        'aa': function (date) {
          return (date.getHours() / 12) >= 1 ? meridiemFull[1] : meridiemFull[0]
        }
      }

      // Generate ordinal version of formatters: M -> Mo, D -> Do, etc.
      var ordinalFormatters = ['M', 'D', 'DDD', 'd', 'Q', 'W']
      ordinalFormatters.forEach(function (formatterToken) {
        formatters[formatterToken + 'o'] = function (date, formatters) {
          return ordinal(formatters[formatterToken](date))
        }
      })

      return {
        formatters: formatters,
        formattingTokensRegExp: buildFormattingTokensRegExp(formatters)
      }
    }
  })();
})();
