import { findWhere } from "underscore";
import {
  holidayFullApprovedCriteria,
  holidayFullTemporaryCriteria,
  holidayFullWaitingCriteria,
  holidayMorningApprovedCriteria,
  holidayMorningTemporaryCriteria,
  holidayMorningWaitingCriteria,
  holidayNoonApprovedCriteria,
  holidayNoonTemporaryCriteria,
  holidayNoonWaitingCriteria,
} from "./absence-types";

const property = (n, v) => ({ name: n, value: v });
const propertyMorning = (v) => property("--absence-bar-color-morning", v);
const propertyNoon = (v) => property("--absence-bar-color-noon", v);
const propertyFull = (v) => property("--absence-bar-color", v);

/**
 *
 * @param node {HTMLElement}
 * @param absences {Array}
 */
export function addAbsenceTypeStyleToNode(node, absences) {
  const properties = getStylePropertiesForDate(absences);
  for (let { name, value } of properties) {
    node.style.setProperty(name, value);
  }
}

/**
 *
 * @param node {HTMLElement}
 */
export function removeAbsenceTypeStyleFromNode(node) {
  node.style.removeProperty("--absence-bar-color-morning");
  node.style.removeProperty("--absence-bar-color-noon");
  node.style.removeProperty("--absence-bar-color");
}

/**
 *
 * @param absences {Array}
 * @returns {{name, value}[]}
 */
function getStylePropertiesForDate(absences) {
  const [colorMorningOrFull, colorNoon] = getVacationTypeColors(absences);
  return [
    findWhere(absences, holidayFullWaitingCriteria) ? propertyFull(colorMorningOrFull) : undefined,
    findWhere(absences, holidayFullTemporaryCriteria) ? propertyFull(colorMorningOrFull) : undefined,
    findWhere(absences, holidayFullApprovedCriteria) ? propertyFull(colorMorningOrFull) : undefined,
    findWhere(absences, holidayMorningWaitingCriteria) ? propertyMorning(colorMorningOrFull) : undefined,
    findWhere(absences, holidayMorningTemporaryCriteria) ? propertyMorning(colorMorningOrFull) : undefined,
    findWhere(absences, holidayMorningApprovedCriteria) ? propertyMorning(colorMorningOrFull) : undefined,
    findWhere(absences, holidayNoonWaitingCriteria) ? propertyNoon(colorNoon) : undefined,
    findWhere(absences, holidayNoonTemporaryCriteria) ? propertyNoon(colorNoon) : undefined,
    findWhere(absences, holidayNoonApprovedCriteria) ? propertyNoon(colorNoon) : undefined,
  ].filter(Boolean);
}

/**
 *
 * @param absences {Array}
 * @returns {string[]}
 */
function getVacationTypeColors(absences) {
  let vacationTypeIdMorningOrFull;
  let vacationTypeIdNoon;

  for (let absence of absences) {
    if (absence.absencePeriodName === "FULL" || absence.absencePeriodName === "MORNING") {
      vacationTypeIdMorningOrFull = absence.vacationTypeId;
    }
    if (absence.absencePeriodName === "NOON") {
      vacationTypeIdNoon = absence.vacationTypeId;
    }
  }

  const colorMorningOrFull = window.uv.vacationTypes.colors[vacationTypeIdMorningOrFull];
  const colorNoon = window.uv.vacationTypes.colors[vacationTypeIdNoon];

  return [colorMorningOrFull, colorNoon];
}
