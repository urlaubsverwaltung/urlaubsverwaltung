import "../vacation-type-select";

describe("vacation-type-select", function () {
  beforeEach(async function () {
    document.body.innerHTML = `
      <select id="vacationType" name="vacationType" is="uv-vacation-type-select">
        <option value="1000" data-vacationtype-category="HOLIDAY">Erholungsurlaub</option>
        <option value="2000" data-vacationtype-category="SPECIALLEAVE">Sonderurlaub</option>
        <option value="3000" data-vacationtype-category="UNPAIDLEAVE">Unbezahlter Urlaub</option>
        <option value="4000" data-vacationtype-category="OVERTIME">Überstundenabbau</option>
      </select>
    `;
  });

  afterEach(function () {
    // cleanup DOM
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
  });

  describe("SPECIAL_LEAVE", function () {
    it("adds 'hidden' class to 'overtime' element", function () {
      const overtimeElement = document.createElement("div");
      overtimeElement.setAttribute("id", "overtime");
      document.body.append(overtimeElement);

      setVacationType("SPECIALLEAVE");

      expect(overtimeElement.classList.contains("hidden")).toBeTruthy();
    });

    it("removes 'hidden' class from 'special-leave' element", function () {
      const specialLeaveElement = document.createElement("div");
      specialLeaveElement.setAttribute("id", "special-leave");
      specialLeaveElement.classList.add("hidden");
      document.body.append(specialLeaveElement);

      setVacationType("SPECIALLEAVE");

      expect(specialLeaveElement.classList.contains("hidden")).toBeFalsy();
    });

    it("does not throw when no element exists", function () {
      expect(() => {
        setVacationType("SPECIALLEAVE");
      }).not.toThrow();
    });
  });

  describe("OVERTIME", function () {
    it("removes 'hidden' class from 'overtime' element", function () {
      const overtimeElement = document.createElement("div");
      overtimeElement.setAttribute("id", "overtime");
      overtimeElement.classList.add("hidden");
      document.body.append(overtimeElement);

      setVacationType("OVERTIME");

      expect(overtimeElement.classList.contains("hidden")).toBeFalsy();
    });

    it("adds 'hidden' class to 'special-leave' element", function () {
      const specialLeaveElement = document.createElement("div");
      specialLeaveElement.setAttribute("id", "special-leave");
      document.body.append(specialLeaveElement);

      setVacationType("OVERTIME");

      expect(specialLeaveElement.classList.contains("hidden")).toBeTruthy();
    });

    it("does not throw when no element exists", function () {
      expect(() => {
        setVacationType("OVERTIME");
      }).not.toThrow();
    });
  });

  describe.each(["HOLIDAY", "UNPAIDLEAVE"])("%s", function (vacationType) {
    it("adds 'hidden' class to 'overtime' element", function () {
      const overtimeElement = document.createElement("div");
      overtimeElement.setAttribute("id", "overtime");
      document.body.append(overtimeElement);

      setVacationType(vacationType);

      expect(overtimeElement.classList.contains("hidden")).toBeTruthy();
    });

    it("adds 'hidden' class to 'special-leave' element", function () {
      const specialLeaveElement = document.createElement("div");
      specialLeaveElement.setAttribute("id", "special-leave");
      document.body.append(specialLeaveElement);

      setVacationType(vacationType);

      expect(specialLeaveElement.classList.contains("hidden")).toBeTruthy();
    });

    it("does not throw when no element exists", function () {
      expect(() => {
        setVacationType(vacationType);
      }).not.toThrow();
    });
  });
});

function setVacationType(vacationType) {
  const value = document.querySelector(`option[data-vacationtype-category='${vacationType}']`).value;
  const selectElement = document.querySelector("#vacationType");
  selectElement.value = value;
  selectElement.dispatchEvent(new Event("change"));
}
