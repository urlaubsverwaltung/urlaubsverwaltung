import { createDatepicker } from "../../../components/datepicker";

vi.mock("../../../components/datepicker", () => ({
  createDatepicker: vi.fn(),
}));

describe("blackout-period-form", () => {
  beforeEach(() => {
    vi.resetModules();
    createDatepicker.mockClear();

    globalThis.uv = { apiPrefix: "api-prefix" };

    document.body.innerHTML = `
      <input id="startDate" />
      <input id="endDate" />
    `;
  });

  afterEach(() => {
    document.body.innerHTML = "";
    delete globalThis.uv;
  });

  it("creates a datepicker for the start date and the end date", async () => {
    await import("../blackout-period-form.js");

    expect(createDatepicker).not.toHaveBeenCalled();

    document.dispatchEvent(new Event("DOMContentLoaded"));

    expect(createDatepicker).toHaveBeenCalledTimes(2);
    expect(createDatepicker).toHaveBeenNthCalledWith(
      1,
      "#startDate",
      expect.objectContaining({ urlPrefix: "api-prefix" }),
    );
    expect(createDatepicker).toHaveBeenNthCalledWith(
      2,
      "#endDate",
      expect.objectContaining({ urlPrefix: "api-prefix" }),
    );
  });

  it("does not resolve a person since blackout periods are not tied to a person", async () => {
    await import("../blackout-period-form.js");

    document.dispatchEvent(new Event("DOMContentLoaded"));

    const [, options] = createDatepicker.mock.calls[0];
    expect(options.getPersonId()).toBeUndefined();
  });

  it("shows the selection of a scope only while 'selected …' is chosen", async () => {
    document.body.innerHTML = `
      <input type="radio" name="companyWide" value="true" data-blackout-scope="departments" checked />
      <input type="radio" name="companyWide" value="false" data-blackout-scope="departments" />
      <div data-blackout-scope-selection="departments" hidden>
        <input type="checkbox" name="departmentIds" value="2" checked />
      </div>
    `;

    const { initScopeSelections } = await import("../blackout-period-form.js");
    initScopeSelections(document);

    const [all, selected] = document.querySelectorAll("[name='companyWide']");
    const selection = document.querySelector("[data-blackout-scope-selection]");

    selected.checked = true;
    selected.dispatchEvent(new Event("change"));
    expect(selection.hidden).toBe(false);

    all.checked = true;
    all.dispatchEvent(new Event("change"));
    expect(selection.hidden).toBe(true);
    // the selection survives hiding
    expect(document.querySelector("[name='departmentIds']").checked).toBe(true);
  });

  describe("department picker", () => {
    beforeEach(() => {
      globalThis.uv.i18n = {
        "blackoutperiod.data.departments.count": "{0} von {1} ausgewählt",
        "blackoutperiod.data.departments.remove": "{0} entfernen",
      };
      document.body.innerHTML = `
        <div data-blackout-departments>
          <div data-blackout-department-chips><span data-blackout-department-none>keine</span></div>
          <template data-blackout-department-chip-template>
            <span><span data-blackout-department-chip-label></span><button data-blackout-department-chip-remove></button></span>
          </template>
          <input type="search" data-blackout-department-search />
          <span data-blackout-department-count></span>
          <button data-blackout-departments-select-all></button>
          <button data-blackout-departments-clear></button>
          <ul>
            <li data-blackout-department data-label="Entwicklung"><input type="checkbox" name="departmentIds" value="1" data-label="Entwicklung" checked /></li>
            <li data-blackout-department data-label="Support"><input type="checkbox" name="departmentIds" value="2" data-label="Support" /></li>
            <li data-blackout-department data-label="Vertrieb"><input type="checkbox" name="departmentIds" value="3" data-label="Vertrieb" /></li>
            <li data-blackout-department-not-found hidden></li>
          </ul>
        </div>
      `;
    });

    const chipLabels = () =>
      [...document.querySelectorAll("[data-blackout-department-chip-label]")].map((label) => label.textContent);
    const checkedValues = () =>
      [...document.querySelectorAll("[name='departmentIds']:checked")].map((checkbox) => checkbox.value);

    it("shows a chip and the count of the selected departments", async () => {
      const { initDepartmentPicker } = await import("../blackout-period-form.js");
      initDepartmentPicker(document);

      expect(chipLabels()).toEqual(["Entwicklung"]);
      expect(document.querySelector("[data-blackout-department-none]").hidden).toBe(true);
      expect(document.querySelector("[data-blackout-department-count]").textContent).toBe("1 von 3 ausgewählt");

      const support = document.querySelector("[value='2']");
      support.checked = true;
      support.dispatchEvent(new Event("change", { bubbles: true }));

      expect(chipLabels()).toEqual(["Entwicklung", "Support"]);
      expect(document.querySelector("[data-blackout-department-count]").textContent).toBe("2 von 3 ausgewählt");
    });

    it("deselects a department with the remove button of its chip", async () => {
      const { initDepartmentPicker } = await import("../blackout-period-form.js");
      initDepartmentPicker(document);

      const remove = document.querySelector("[data-blackout-department-chip-remove]");
      expect(remove.getAttribute("aria-label")).toBe("Entwicklung entfernen");
      remove.click();

      expect(checkedValues()).toEqual([]);
      expect(chipLabels()).toEqual([]);
      expect(document.querySelector("[data-blackout-department-none]").hidden).toBe(false);
    });

    it("filters the departments and selects only the found ones", async () => {
      const { initDepartmentPicker } = await import("../blackout-period-form.js");
      initDepartmentPicker(document);

      const search = document.querySelector("[data-blackout-department-search]");
      search.value = "SUP";
      search.dispatchEvent(new Event("input"));

      const hidden = [...document.querySelectorAll("[data-blackout-department]")].map((row) => row.hidden);
      expect(hidden).toEqual([true, false, true]);

      document.querySelector("[data-blackout-departments-select-all]").click();
      expect(checkedValues()).toEqual(["1", "2"]);

      search.value = "Marketing";
      search.dispatchEvent(new Event("input"));
      expect(document.querySelector("[data-blackout-department-not-found]").hidden).toBe(false);
    });

    it("tells the form about selecting all departments with a single change event", async () => {
      const { initDepartmentPicker } = await import("../blackout-period-form.js");
      initDepartmentPicker(document);
      const changes = vi.fn();
      document.addEventListener("change", changes);

      document.querySelector("[data-blackout-departments-select-all]").click();
      expect(changes).toHaveBeenCalledTimes(1);
      expect(checkedValues()).toEqual(["1", "2", "3"]);

      document.querySelector("[data-blackout-departments-clear]").click();
      expect(changes).toHaveBeenCalledTimes(2);

      // nothing changes, nothing to tell
      document.querySelector("[data-blackout-departments-clear]").click();
      expect(changes).toHaveBeenCalledTimes(2);

      document.removeEventListener("change", changes);
    });

    it("clears the selection", async () => {
      const { initDepartmentPicker } = await import("../blackout-period-form.js");
      initDepartmentPicker(document);

      document.querySelector("[data-blackout-departments-clear]").click();

      expect(checkedValues()).toEqual([]);
    });
  });

  describe("impact", () => {
    const persons = (count) =>
      Array.from(
        { length: count },
        (_, index) => `<li data-blackout-person data-department-ids="9">Person ${index}</li>`,
      ).join("");

    const render = ({ companyWide = false, allVacationTypes = false, morePersons = "" } = {}) => {
      document.body.innerHTML = `
        <form>
          <input type="radio" name="companyWide" value="true" ${companyWide ? "checked" : ""} />
          <input type="radio" name="companyWide" value="false" ${companyWide ? "" : "checked"} />
          <input type="checkbox" name="departmentIds" value="1" data-label="Entwicklung" checked />
          <input type="checkbox" name="departmentIds" value="2" data-label="Support" checked />
          <input type="checkbox" name="departmentIds" value="3" data-label="Vertrieb" />
          <input type="radio" name="allVacationTypes" value="true" ${allVacationTypes ? "checked" : ""} />
          <input type="radio" name="allVacationTypes" value="false" ${allVacationTypes ? "" : "checked"} />
          <input type="checkbox" name="vacationTypeIds" value="10" data-label="Erholungsurlaub" checked />
          <input type="checkbox" name="vacationTypeIds" value="11" data-label="Überstundenabbau" checked />
          <input type="checkbox" name="vacationTypeIds" value="12" data-label="Sonderurlaub" />
          <input type="hidden" name="_csrf" value="secret" />
          <div data-blackout-impact>
            <p><span data-blackout-impact-sentence></span><span data-blackout-impact-on-behalf></span></p>
            <turbo-frame data-blackout-conflicts-frame data-src="/web/blackoutperiod/conflicts"></turbo-frame>
            <div data-blackout-impact-persons>
              <h3 data-blackout-impact-persons-heading></h3>
              <ul>
                <li data-blackout-person data-department-ids="1">Anna</li>
                <li data-blackout-person data-department-ids="1 2">Lea</li>
                <li data-blackout-person data-department-ids="2">Paul</li>
                <li data-blackout-person data-department-ids="3">Max</li>
                <li data-blackout-person>Tom</li>
                ${morePersons}
                <li><button type="button" data-blackout-impact-persons-toggle></button></li>
              </ul>
            </div>
          </div>
        </form>
      `;
    };

    const sentence = () => document.querySelector("[data-blackout-impact-sentence]");
    const shownPersons = () =>
      [...document.querySelectorAll("[data-blackout-person]")]
        .filter((row) => !row.hidden)
        .map((row) => row.textContent);

    beforeEach(() => {
      document.documentElement.lang = "de";
      globalThis.uv.i18n = {
        "blackoutperiod.impact.persons.one": "{0} Person",
        "blackoutperiod.impact.persons.other": "{0} Personen",
        "blackoutperiod.impact.who.companyWide": "Alle {0} im Unternehmen",
        "blackoutperiod.impact.who.departments": "{0} aus {1}",
        "blackoutperiod.impact.sentence.one": "{0} kann {1} {2} nicht selbst beantragen.",
        "blackoutperiod.impact.sentence.other": "{0} können {1} {2} nicht selbst beantragen.",
        "blackoutperiod.impact.period.range": "vom {0} bis {1}",
        "blackoutperiod.impact.period.day": "am {0}",
        "blackoutperiod.impact.period.unknown": "im gewählten Zeitraum",
        "blackoutperiod.impact.vacationTypes.all": "Urlaub jeder Art",
        "blackoutperiod.impact.vacationType": "„{0}“",
        "blackoutperiod.impact.and": "und",
        "blackoutperiod.impact.nobody": "niemand",
        "blackoutperiod.impact.incomplete": "unvollständig",
        "blackoutperiod.impact.persons.heading": "Betroffene Personen ({0})",
        "blackoutperiod.impact.persons.more": "+ {0} weitere anzeigen",
        "blackoutperiod.impact.persons.less": "Weniger anzeigen",
      };
    });

    afterEach(() => {
      document.documentElement.lang = "";
    });

    it("describes who cannot request which vacation types and lists each affected person once", async () => {
      render();
      const { initImpact } = await import("../blackout-period-form.js");
      initImpact(document, { startDate: "2026-12-22", endDate: "2027-01-06" });

      expect(sentence().textContent).toBe(
        "3 Personen aus Entwicklung und Support können vom 22.12.2026 bis 06.01.2027 „Erholungsurlaub“ und „Überstundenabbau“ nicht selbst beantragen.",
      );
      expect([...sentence().querySelectorAll("strong")].map((element) => element.textContent)).toEqual([
        "3 Personen aus Entwicklung und Support",
        "„Erholungsurlaub“ und „Überstundenabbau“",
      ]);
      expect(shownPersons()).toEqual(["Anna", "Lea", "Paul"]);
      expect(document.querySelector("[data-blackout-impact-persons-heading]").textContent).toBe(
        "Betroffene Personen (3)",
      );
    });

    it("affects every person company-wide and every vacation type", async () => {
      render({ companyWide: true, allVacationTypes: true });
      const { initImpact } = await import("../blackout-period-form.js");
      initImpact(document, { startDate: "2026-12-24", endDate: "2026-12-24" });

      expect(sentence().textContent).toBe(
        "Alle 5 Personen im Unternehmen können am 24.12.2026 Urlaub jeder Art nicht selbst beantragen.",
      );
      expect(shownPersons()).toEqual(["Anna", "Lea", "Paul", "Max", "Tom"]);
    });

    it("updates with the selection and the dates", async () => {
      render();
      const { initImpact } = await import("../blackout-period-form.js");
      initImpact(document, { startDate: "", endDate: "" });

      expect(sentence().textContent).toContain("im gewählten Zeitraum");

      const support = document.querySelector("[name='departmentIds'][value='2']");
      support.checked = false;
      support.dispatchEvent(new Event("change", { bubbles: true }));

      const startDate = document.createElement("duet-date-picker");
      startDate.setAttribute("name", "startDate");
      const endDate = document.createElement("duet-date-picker");
      endDate.setAttribute("name", "endDate");
      document.body.append(startDate, endDate);
      startDate.dispatchEvent(new CustomEvent("duetChange", { bubbles: true, detail: { value: "2026-12-22" } }));
      endDate.dispatchEvent(new CustomEvent("duetChange", { bubbles: true, detail: { value: "2027-01-06" } }));

      expect(sentence().textContent).toBe(
        "2 Personen aus Entwicklung können vom 22.12.2026 bis 06.01.2027 „Erholungsurlaub“ und „Überstundenabbau“ nicht selbst beantragen.",
      );
      expect(shownPersons()).toEqual(["Anna", "Lea"]);
    });

    it("uses the singular for one affected person", async () => {
      render();
      document.querySelector("[name='departmentIds'][value='1']").checked = false;
      document.querySelector("[name='departmentIds'][value='2']").checked = false;
      document.querySelector("[name='departmentIds'][value='3']").checked = true;
      const { initImpact } = await import("../blackout-period-form.js");
      initImpact(document, { startDate: "", endDate: "" });

      expect(sentence().textContent).toBe(
        "1 Person aus Vertrieb kann im gewählten Zeitraum „Erholungsurlaub“ und „Überstundenabbau“ nicht selbst beantragen.",
      );
    });

    it("asks for a department and a vacation type when none is selected", async () => {
      render();
      for (const checkbox of document.querySelectorAll("[name='vacationTypeIds']")) {
        checkbox.checked = false;
      }
      const { initImpact } = await import("../blackout-period-form.js");
      initImpact(document, { startDate: "", endDate: "" });

      expect(sentence().textContent).toBe("unvollständig");
      expect(document.querySelector("[data-blackout-impact-persons]").hidden).toBe(true);
      expect(document.querySelector("[data-blackout-impact-on-behalf]").hidden).toBe(true);
    });

    it("tells when nobody is in the selected departments", async () => {
      render();
      document.querySelector("[name='departmentIds'][value='1']").checked = false;
      document.querySelector("[name='departmentIds'][value='2']").checked = false;
      document.querySelector("[data-blackout-person][data-department-ids='3']").dataset.departmentIds = "4";
      document.querySelector("[name='departmentIds'][value='3']").checked = true;
      const { initImpact } = await import("../blackout-period-form.js");
      initImpact(document, { startDate: "", endDate: "" });

      expect(sentence().textContent).toBe("niemand");
      expect(document.querySelector("[data-blackout-impact-persons]").hidden).toBe(true);
    });

    it("collapses the affected persons after twelve and expands them on demand", async () => {
      render({ companyWide: true, morePersons: persons(10) });
      const { initImpact } = await import("../blackout-period-form.js");
      initImpact(document, { startDate: "", endDate: "" });

      const toggle = document.querySelector("[data-blackout-impact-persons-toggle]");
      expect(shownPersons()).toHaveLength(12);
      expect(toggle.textContent).toBe("+ 3 weitere anzeigen");
      expect(toggle.getAttribute("aria-expanded")).toBe("false");

      toggle.click();

      expect(shownPersons()).toHaveLength(15);
      expect(toggle.textContent).toBe("Weniger anzeigen");
      expect(toggle.getAttribute("aria-expanded")).toBe("true");
    });

    it("loads the colliding applications for the scope and the dates without the csrf token", async () => {
      render();
      const { initImpact } = await import("../blackout-period-form.js");
      initImpact(document, { startDate: "2026-12-22", endDate: "2027-01-06" });

      const frame = document.querySelector("[data-blackout-conflicts-frame]");
      const [path, query] = frame.src.split("?");
      const parameters = new URLSearchParams(query);
      expect(path).toBe("/web/blackoutperiod/conflicts");
      expect(parameters.get("companyWide")).toBe("false");
      expect(parameters.getAll("departmentIds")).toEqual(["1", "2"]);
      expect(parameters.get("allVacationTypes")).toBe("false");
      expect(parameters.getAll("vacationTypeIds")).toEqual(["10", "11"]);
      expect(parameters.get("startDate")).toBe("2026-12-22");
      expect(parameters.get("endDate")).toBe("2027-01-06");
      expect(parameters.has("_csrf")).toBe(false);

      const support = document.querySelector("[name='departmentIds'][value='2']");
      support.checked = false;
      support.dispatchEvent(new Event("change", { bubbles: true }));

      expect(new URLSearchParams(frame.src.split("?")[1]).getAll("departmentIds")).toEqual(["1"]);
    });

    it("hides the expand button when all affected persons are shown", async () => {
      render();
      const { initImpact } = await import("../blackout-period-form.js");
      initImpact(document, { startDate: "", endDate: "" });

      expect(document.querySelector("[data-blackout-impact-persons-toggle]").parentElement.hidden).toBe(true);
    });
  });
});
