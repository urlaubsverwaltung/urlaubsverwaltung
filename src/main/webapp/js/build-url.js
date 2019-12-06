export default function buildUrl(urlPrefix, startDate, endDate, dayLength, personId) {

  return urlPrefix + "?from=" + startDate + "&to=" + endDate + "&length=" + dayLength + "&person=" + personId;
}
