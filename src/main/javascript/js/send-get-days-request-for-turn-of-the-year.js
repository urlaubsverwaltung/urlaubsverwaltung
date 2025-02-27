// disabling date-fns#format is ok since we're formatting dates for api requests
// eslint-disable-next-line no-restricted-imports
import { format, getYear, isAfter } from "date-fns";
import formatNumber from "./format-number";
import { getJSON } from "./fetch";

export default async function sendGetDaysRequestForTurnOfTheYear(
  urlPrefix,
  startDate,
  toDate,
  dayLength,
  personId,
  elementSelector,
) {
  const element = document.querySelector(elementSelector);
  element.innerHTML = "";

  if ((!personId && !startDate && !toDate) || isAfter(startDate, toDate)) {
    return;
  }

  const ranges = getYearlyDateRanges(startDate, toDate);
  const promises = ranges.map((range) =>
    getWorkdaysForDateRange(urlPrefix, dayLength, personId, range.start, range.end),
  );
  const results = await Promise.all(promises);

  const lastResult = results.pop();
  const formattedResults = results.map((result) => `${formatNumber(result.workDays)} in ${result.year}`);
  element.innerHTML = `<br />(${formattedResults.join(", ")} und ${formatNumber(lastResult.workDays)} in ${lastResult.year})`;
}

function getYearlyDateRanges(startDate, endDate) {
  const yearlyRanges = [];

  let currentYear = getYear(startDate);
  let currentStart = startDate;

  while (currentStart < endDate) {
    let currentEnd = new Date(currentYear + 1, 0, 1);
    currentEnd.setDate(currentEnd.getDate() - 1);

    if (currentEnd > endDate) {
      currentEnd = endDate;
    }

    yearlyRanges.push({
      start: currentStart,
      end: currentEnd,
    });

    // Move to the next year
    currentYear++;
    currentStart = new Date(currentYear, 0, 1);
  }

  return yearlyRanges;
}

async function getWorkdaysForDateRange(urlPrefix, dayLength, personId, fromDate, toDate) {
  const startDate = format(fromDate, "yyyy-MM-dd");
  const endDate = format(toDate, "yyyy-MM-dd");
  let url = urlPrefix + "/persons/" + personId + "/workdays?from=" + startDate + "&to=" + endDate;
  if (dayLength) {
    url = url + "&length=" + dayLength;
  }

  const json = await getJSON(url);

  return {
    year: getYear(fromDate),
    workDays: json.workDays,
  };
}
