import { isPublicHoliday, isPublicHolidayMorning, isPublicHolidayNoon } from "../../js/public-holiday";
import {
  isPersonalHolidayApprovedFull,
  isPersonalHolidayApprovedMorning,
  isPersonalHolidayApprovedNoon,
  isPersonalHolidayCancellationRequestedFull,
  isPersonalHolidayCancellationRequestedMorning,
  isPersonalHolidayCancellationRequestedNoon,
  isPersonalHolidayTemporaryFull,
  isPersonalHolidayTemporaryMorning,
  isPersonalHolidayTemporaryNoon,
  isPersonalHolidayWaitingFull,
  isPersonalHolidayWaitingMorning,
  isPersonalHolidayWaitingNoon,
  isSickNoteFull,
  isSickNoteMorning,
  isSickNoteNoon,
} from "../../js/absence";
import { isToday, isWeekend } from "date-fns";

const css = {
  day: "datepicker-day",
  today: "datepicker-day-today",
  past: "datepicker-day-past",
  weekend: "datepicker-day-weekend",
  publicHolidayFull: "datepicker-day-public-holiday-full",
  publicHolidayMorning: "datepicker-day-public-holiday-morning",
  publicHolidayNoon: "datepicker-day-public-holiday-noon",
  absenceFull: "datepicker-day-absence-full",
  absenceFullSolid: "absence-full--solid",
  absenceFullOutline: "absence-full--outline",
  absenceFullOutlineSolidHalf: "absence-full--outline-solid-half",
  absenceFullOutlineSolidSecondHalf: "absence-full--outline-solid-second-half",
  absenceMorning: "datepicker-day-absence-morning",
  absenceMorningSolid: "absence-morning--solid",
  absenceMorningOutline: "absence-morning--outline",
  absenceMorningOutlineSolidHalf: "absence-morning--outline-solid-half",
  absenceMorningOutlineSolidSecondHalf: "absence-morning--outline-solid-second-half",
  absenceNoon: "datepicker-day-absence-noon",
  absenceNoonSolid: "absence-noon--solid",
  absenceNoonOutline: "absence-noon--outline",
  absenceNoonOutlineSolidHalf: "absence-noon--outline-solid-half",
  absenceNoonOutlineSolidSecondHalf: "absence-noon--outline-solid-second-half",
  sickNoteFull: "datepicker-day-sick-note-full",
  sickNoteMorning: "datepicker-day-sick-note-morning",
  sickNoteNoon: "datepicker-day-sick-note-noon",
};

const isPast = () => false;

/**
 *
 * @param node {HTMLElement}
 */
export function removeDatepickerCssClassesFromNode(node) {
  node.classList.remove(...Object.values(css));
}

/**
 *
 * @param node {HTMLElement}
 * @param date {Date}
 * @param absences {Array}
 * @param publicHolidays {Array}
 */
export function addDatepickerCssClassesToNode(node, date, absences, publicHolidays) {
  const cssClasses = getCssClassesForDate(date, absences, publicHolidays);
  node.classList.add(...cssClasses);
}

function getCssClassesForDate(date, absences, publicHolidays) {
  return [
    css.day,
    isToday(date) && css.today,
    isPast() && css.past,
    isWeekend(date) && css.weekend,
    isPublicHoliday(publicHolidays) && css.publicHolidayFull,
    isPublicHolidayMorning(publicHolidays) && css.publicHolidayMorning,
    isPublicHolidayNoon(publicHolidays) && css.publicHolidayNoon,

    isPersonalHolidayWaitingFull(absences) && [css.absenceFull, css.absenceFullOutline],
    isPersonalHolidayTemporaryFull(absences) && [css.absenceFull, css.absenceFullOutlineSolidHalf],
    isPersonalHolidayApprovedFull(absences) && [css.absenceFull, css.absenceFullSolid],
    isPersonalHolidayCancellationRequestedFull(absences) && [css.absenceFull, css.absenceFullOutlineSolidSecondHalf],

    isPersonalHolidayWaitingMorning(absences) && [css.absenceMorning, css.absenceMorningOutline],
    isPersonalHolidayTemporaryMorning(absences) && [css.absenceMorning, css.absenceMorningOutlineSolidHalf],
    isPersonalHolidayApprovedMorning(absences) && [css.absenceMorning, css.absenceMorningSolid],
    isPersonalHolidayCancellationRequestedMorning(absences) && [
      css.absenceMorning,
      css.absenceMorningOutlineSolidSecondHalf,
    ],

    isPersonalHolidayWaitingNoon(absences) && [css.absenceNoon, css.absenceNoonOutline],
    isPersonalHolidayTemporaryNoon(absences) && [css.absenceNoon, css.absenceNoonOutlineSolidHalf],
    isPersonalHolidayApprovedNoon(absences) && [css.absenceNoon, css.absenceNoonSolid],
    isPersonalHolidayCancellationRequestedNoon(absences) && [css.absenceNoon, css.absenceNoonOutlineSolidSecondHalf],

    isSickNoteFull(absences) && css.sickNoteFull,
    isSickNoteMorning(absences) && css.sickNoteMorning,
    isSickNoteNoon(absences) && css.sickNoteNoon,
  ]
    .flat()
    .filter(Boolean);
}
