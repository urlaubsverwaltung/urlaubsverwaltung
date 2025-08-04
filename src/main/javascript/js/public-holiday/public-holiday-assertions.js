export function isPublicHoliday(publicHolidays) {
  return publicHolidays.some((holiday) => holiday.absencePeriodName === "FULL");
}

export function isPublicHolidayMorning(publicHolidays) {
  return publicHolidays.some((holiday) => holiday.absencePeriodName === "MORNING");
}

export function isPublicHolidayNoon(publicHolidays) {
  return publicHolidays.some((holiday) => holiday.absencePeriodName === "NOON");
}
