import { getYear, isAfter } from "date-fns";
import formatNumber from "./format-number";
import { getWorkdays } from "./get-workdays";

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
  const promises = ranges.map(async (range) => ({
    year: getYear(range.start),
    workDays: await getWorkdays(urlPrefix, dayLength, personId, range.start, range.end),
  }));
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
