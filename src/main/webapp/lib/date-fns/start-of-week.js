import dateFnsStartOfWeek from 'date-fns/start_of_week'

export default function startOfWeek(date, options = {}) {
  if (options.weekStartsOn == undefined) {
    options.weekStartsOn = window.uv.weekStartsOn;
  }

  return dateFnsStartOfWeek(date, options);
}
