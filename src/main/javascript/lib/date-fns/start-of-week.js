import dateFnsStartOfWeek from 'date-fns/startOfWeek'

export default function startOfWeek(date, options = {}) {
  if (options.weekStartsOn == undefined) {
    options.weekStartsOn = window.uv.weekStartsOn;
  }

  return dateFnsStartOfWeek(date, options);
}
