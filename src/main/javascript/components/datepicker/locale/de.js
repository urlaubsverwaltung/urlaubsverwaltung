import format from "../../../lib/date-fns/format";

const DATE_FORMAT = /^(\d{1,2})\.(\d{1,2})\.(\d{4})$/;

export const dateFormat = "dd.MM.yyyy";

export const dateAdapter = {
  parse(value, createDate) {
    const matches = (value || "").match(DATE_FORMAT);

    if (matches) {
      return createDate(matches[3], matches[2], matches[1]);
    }
  },
  format(date) {
    return date ? format(date, dateFormat) : "";
  },
};

export default { dateFormat, dateAdapter };
