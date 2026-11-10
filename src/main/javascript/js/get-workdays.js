// disabling date-fns#format is ok since we're formatting dates for api requests
// eslint-disable-next-line no-restricted-imports
import { format } from "date-fns";
import { getJSON } from "./fetch";

export async function getWorkdays(urlPrefix, dayLength, personId, fromDate, toDate) {
  const startDate = format(fromDate, "yyyy-MM-dd");
  const endDate = format(toDate, "yyyy-MM-dd");
  let url = urlPrefix + "/persons/" + personId + "/workdays?from=" + startDate + "&to=" + endDate;
  if (dayLength) {
    url = url + "&length=" + dayLength;
  }

  const json = await getJSON(url);
  return json.workDays;
}
