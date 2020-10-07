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

export const localization = {
  buttonLabel: "Wähle einen Tag aus",
  placeholder: "TT.MM.JJJJ",
  selectedDateMessage: "ausgewählt ist",
  prevMonthLabel: "voriger Monat",
  nextMonthLabel: "nächster Monat",
  monthSelectLabel: "Monat",
  yearSelectLabel: "Jahr",
  closeLabel: "Schließen",
  keyboardInstruction: "Du kannst die Pfeiltasten nutzen um ein Datum auszuwählen.",
  calendarHeading: "Wähle einen Tag",
  dayNames: ["Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag"],
  monthNames: [
    "Januar",
    "Februar",
    "März",
    "April",
    "Mai",
    "Juni",
    "Juli",
    "August",
    "September",
    "Oktober",
    "November",
    "Dezember",
  ],
  monthNamesShort: ["Jan", "Feb", "März", "Apr", "Mai", "Juni", "Juli", "Aug", "Sep", "Okt", "Nov", "Dez"],
};

export default { dateFormat, dateAdapter, localization };
