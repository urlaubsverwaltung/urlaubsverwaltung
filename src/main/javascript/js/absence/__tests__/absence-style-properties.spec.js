import { addAbsenceTypeStyleToNode, removeAbsenceTypeStyleFromNode } from "../absence-style-properties";

describe("absence-style-properties", () => {
  describe("addAbsenceTypeStyleToNode", () => {
    beforeEach(() => {
      globalThis.uv = {};
      globalThis.uv.vacationTypes = {};
      globalThis.uv.vacationTypes.colors = {};
      globalThis.uv.vacationTypes.colors[1] = "color-1";
      globalThis.uv.vacationTypes.colors[2] = "color-2";
      globalThis.uv.vacationTypes.colors[3] = "color-3";
    });

    it.each([
      [morningTemporary(1), "color-1"],
      [morningApproved(1), "color-1"],
      [morningWaiting(1), "color-1"],
      [morningTemporary(3), "color-3"],
      [morningApproved(3), "color-3"],
      [morningWaiting(3), "color-3"],
    ])("adds 'morning' style property", (givenAbsence, expectedColorValue) => {
      const div = document.createElement("div");
      const absences = [givenAbsence];

      addAbsenceTypeStyleToNode(div, absences);

      expect(div.style.getPropertyValue("--absence-bar-color-morning")).toBe(
        `var(--absence-color-${expectedColorValue})`,
      );
    });

    it.each([
      [noonTemporary(1), "color-1"],
      [noonApproved(1), "color-1"],
      [noonWaiting(1), "color-1"],
      [noonTemporary(3), "color-3"],
      [noonApproved(3), "color-3"],
      [noonWaiting(3), "color-3"],
    ])("adds 'noon' style property", (givenAbsence, expectedColorValue) => {
      const div = document.createElement("div");
      const absences = [givenAbsence];

      addAbsenceTypeStyleToNode(div, absences);

      expect(div.style.getPropertyValue("--absence-bar-color-noon")).toBe(`var(--absence-color-${expectedColorValue})`);
    });

    it.each([
      [morningTemporary(1), "color-1", noonTemporary(2), "color-2"],
      [morningApproved(1), "color-1", noonApproved(2), "color-2"],
      [morningWaiting(1), "color-1", noonWaiting(2), "color-2"],
      [morningTemporary(2), "color-2", noonTemporary(3), "color-3"],
      [morningApproved(2), "color-2", noonApproved(3), "color-3"],
      [morningWaiting(2), "color-2", noonWaiting(3), "color-3"],
    ])("adds 'morning' and 'noon' style property", (morning, expectMorningColor, noon, expectedNoonColor) => {
      const div = document.createElement("div");
      const absences = [morning, noon];

      addAbsenceTypeStyleToNode(div, absences);

      expect(div.style.getPropertyValue("--absence-bar-color-morning")).toBe(
        `var(--absence-color-${expectMorningColor})`,
      );
      expect(div.style.getPropertyValue("--absence-bar-color-noon")).toBe(`var(--absence-color-${expectedNoonColor})`);
    });

    it.each([
      [fullTemporary(1), "color-1"],
      [fullApproved(1), "color-1"],
      [fullWaiting(1), "color-1"],
      [fullTemporary(3), "color-3"],
      [fullApproved(3), "color-3"],
      [fullWaiting(3), "color-3"],
    ])("adds 'full' style property", (givenAbsence, expectedColorValue) => {
      const div = document.createElement("div");
      const absences = [givenAbsence];

      addAbsenceTypeStyleToNode(div, absences);

      expect(div.style.getPropertyValue("--absence-bar-color")).toBe(`var(--absence-color-${expectedColorValue})`);
    });
  });

  describe("removeAbsenceTypeStyleFromNode", () => {
    it.each([["--absence-bar-color-morning"], ["--absence-bar-color-noon"], ["--absence-bar-color"]])(
      "removes style property '%s'",
      (givenStyleProperty) => {
        const div = document.createElement("div");
        div.style.setProperty(givenStyleProperty, "orange");
        expect(div.style.getPropertyValue(givenStyleProperty)).toBe("orange");

        removeAbsenceTypeStyleFromNode(div);

        expect(div.style.getPropertyValue(givenStyleProperty)).toBe("");
      },
    );
  });
});

function morningWaiting(typeId) {
  return {
    typeId,
    absenceType: "VACATION",
    status: "WAITING",
    absent: "MORNING",
  };
}
function morningApproved(typeId) {
  return {
    typeId,
    absenceType: "VACATION",
    status: "ALLOWED",
    absent: "MORNING",
  };
}
function morningTemporary(typeId) {
  return {
    typeId,
    absenceType: "VACATION",
    status: "TEMPORARY_ALLOWED",
    absent: "MORNING",
  };
}
function noonWaiting(typeId) {
  return {
    typeId,
    absenceType: "VACATION",
    status: "WAITING",
    absent: "NOON",
  };
}
function noonApproved(typeId) {
  return {
    typeId,
    absenceType: "VACATION",
    status: "ALLOWED",
    absent: "NOON",
  };
}
function noonTemporary(typeId) {
  return {
    typeId,
    absenceType: "VACATION",
    status: "TEMPORARY_ALLOWED",
    absent: "NOON",
  };
}
function fullWaiting(typeId) {
  return {
    typeId,
    absenceType: "VACATION",
    status: "WAITING",
    absent: "FULL",
  };
}
function fullApproved(typeId) {
  return {
    typeId,
    absenceType: "VACATION",
    status: "ALLOWED",
    absent: "FULL",
  };
}
function fullTemporary(typeId) {
  return {
    typeId,
    absenceType: "VACATION",
    status: "TEMPORARY_ALLOWED",
    absent: "FULL",
  };
}
