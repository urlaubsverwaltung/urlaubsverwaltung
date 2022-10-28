import format from "../../../lib/date-fns/format";
import { localisation as de } from "./de";
// eslint-disable-next-line unicorn/prevent-abbreviations
import { localisation as el } from "./el";
import { localisation as en } from "./en";
import { localisation as enGB } from "./en-gb";

const EN_MONTH_DAY_LOCALES = new Set(["en", "en-CA", "en-CB", "en-PH", "en-US"]);
const EN_DAY_MONTH_LOCALES = new Set(["en-AU", "en-BZ", "en-GB", "en-IN", "en-IE", "en-JM", "en-NZ", "en-ZA", "en-TT"]);

export function createDatepickerLocalization({ locale }) {
  let localisation;

  if (EN_MONTH_DAY_LOCALES.has(locale)) {
    localisation = en;
  } else if (EN_DAY_MONTH_LOCALES.has(locale)) {
    localisation = enGB;
  } else if (locale === "el") {
    localisation = el;
  } else {
    localisation = de;
  }

  const { dateFormat, dateFormatShort, dateFormatPattern, createDate } = localisation;

  return {
    /**
     * format used for the visually visible date string of the input element.
     * @type {string}
     */
    dateFormat,

    /**
     * short date format used by duet-date-picker for screen reader description only.
     *
     * note that duet does not use this pattern to format a date! duet has a hard coded one!
     * we're (mis)using this to enhance the date-picker visuals :x
     * therefore this value has to match the <a href="https://github.com/duetds/date-picker/blob/v1.2.0/src/components/duet-date-picker/duet-date-picker.tsx#L219">duet-date-picker implementation</a>!
     *
     *  @type {string}
     */
    dateFormatShort,

    /**
     * duet-date-picker adapter to format and parse user input.
     */
    dateAdapter: {
      parse(value, duetCreateDate) {
        const matches = (value || "").match(dateFormatPattern);
        if (matches) {
          return createDate(matches, duetCreateDate);
        }
      },
      format(date) {
        return date ? format(date, dateFormat) : "";
      },
    },
  };
}
