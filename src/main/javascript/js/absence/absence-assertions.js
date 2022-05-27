import { findWhere } from "underscore";
import {
  noWorkdayCriteria,
  sickNoteFullCriteria,
  sickNoteMorningCriteria,
  sickNoteNoonCriteria,
  holidayFullWaitingCriteria,
  holidayMorningWaitingCriteria,
  holidayNoonWaitingCriteria,
  holidayFullTemporaryCriteria,
  holidayMorningTemporaryCriteria,
  holidayNoonTemporaryCriteria,
  holidayFullApprovedCriteria,
  holidayMorningApprovedCriteria,
  holidayNoonApprovedCriteria,
  holidayFullCancellationRequestedCriteria,
  holidayMorningCancellationRequestedCriteria,
  holidayNoonCancellationRequestedCriteria,
} from "./absence-criteria";

export function isNoWorkday(absences) {
  return Boolean(findWhere(absences, noWorkdayCriteria));
}

export function isSickNoteFull(absences) {
  return Boolean(findWhere(absences, sickNoteFullCriteria));
}

export function isSickNoteMorning(absences) {
  return Boolean(findWhere(absences, sickNoteMorningCriteria));
}

export function isSickNoteNoon(absences) {
  return Boolean(findWhere(absences, sickNoteNoonCriteria));
}

export function isPersonalHolidayWaitingFull(absences) {
  return Boolean(findWhere(absences, holidayFullWaitingCriteria));
}

export function isPersonalHolidayWaitingMorning(absences) {
  return Boolean(findWhere(absences, holidayMorningWaitingCriteria));
}

export function isPersonalHolidayWaitingNoon(absences) {
  return Boolean(findWhere(absences, holidayNoonWaitingCriteria));
}

export function isPersonalHolidayTemporaryFull(absences) {
  return Boolean(findWhere(absences, holidayFullTemporaryCriteria));
}

export function isPersonalHolidayTemporaryMorning(absences) {
  return Boolean(findWhere(absences, holidayMorningTemporaryCriteria));
}

export function isPersonalHolidayTemporaryNoon(absences) {
  return Boolean(findWhere(absences, holidayNoonTemporaryCriteria));
}

export function isPersonalHolidayApprovedFull(absences) {
  return Boolean(findWhere(absences, holidayFullApprovedCriteria));
}

export function isPersonalHolidayApprovedMorning(absences) {
  return Boolean(findWhere(absences, holidayMorningApprovedCriteria));
}

export function isPersonalHolidayApprovedNoon(absences) {
  return Boolean(findWhere(absences, holidayNoonApprovedCriteria));
}

export function isPersonalHolidayCancellationRequestedFull(absences) {
  return Boolean(findWhere(absences, holidayFullCancellationRequestedCriteria));
}

export function isPersonalHolidayCancellationRequestedMorning(absences) {
  return Boolean(findWhere(absences, holidayMorningCancellationRequestedCriteria));
}

export function isPersonalHolidayCancellationRequestedNoon(absences) {
  return Boolean(findWhere(absences, holidayNoonCancellationRequestedCriteria));
}
