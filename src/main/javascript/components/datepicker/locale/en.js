export const localisation = {
  /**
   * Regex used to parse the user input into a date object.
   *
   * @type {RegExp}
   */
  // the same as german since we support this format only currently
  dateFormatPattern: /^(\d{1,2})\.(\d{1,2})\.(\d{4})$/,

  /**
   * format used for the visually visible date string of the input element.
   * @type {string}
   */
  dateFormat: "d.M.yyyy",

  /**
   * short date format used by duet-date-picker for screen reader description only.
   *
   * note that duet does not use this pattern to format a date! duet has a hard coded one!
   * we're (mis)using this to enhance the date-picker visuals :x
   * therefore this value has to match the <a href="https://github.com/duetds/date-picker/blob/v1.2.0/src/components/duet-date-picker/duet-date-picker.tsx#L219">duet-date-picker implementation</a>!
   *
   *  @type {string}
   */
  dateFormatShort: "MMMM dd",

  /**
   * Map the user input (string value) to a date object.
   *
   * @param matches array of values matched the <code>dateFormatPattern</code>
   * @param createDate createDate function of the duet-date-picker which handles stuff like checking day between 1-31.
   * @return {Date|undefined}
   */
  createDate(matches, createDate) {
    return createDate(matches[3], matches[2], matches[1]);
  },
};
