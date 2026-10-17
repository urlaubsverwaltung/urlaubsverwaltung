import "./department-picker";

describe("department-picker", function () {
  afterEach(function () {
    document.body.innerHTML = "";
  });

  function render({ all = false, selected = [], departments = ["Admins", "Marketing", "Vertrieb"] } = {}) {
    document.body.innerHTML = `
      <form id="host">
        <uv-department-picker>
          <button type="button" popovertarget="picker-popover">Abteilungen</button>
          <div id="picker-popover" popover>
            <input type="search" data-department-picker-search />
            <input type="checkbox" name="allPersons" value="true" data-department-picker-all ${all ? "checked" : ""} />
            <ul>
              ${departments
                .map(
                  (name) =>
                    `<li><input type="checkbox" name="department" value="${name}" data-department-picker-department ${
                      selected.includes(name) ? "checked" : ""
                    } /></li>`,
                )
                .join("")}
              <li data-department-picker-no-hit hidden>Keine Abteilung gefunden.</li>
            </ul>
            <button type="submit" data-department-picker-confirm>Bestätigen</button>
          </div>
        </uv-department-picker>
      </form>
    `;

    const form = document.querySelector("#host");
    form.addEventListener("submit", (event) => event.preventDefault());

    return {
      form,
      popover: document.querySelector("[popover]"),
      all: document.querySelector("[data-department-picker-all]"),
      department: (name) => document.querySelector(`[data-department-picker-department][value="${name}"]`),
      search: document.querySelector("[data-department-picker-search]"),
      noHit: document.querySelector("[data-department-picker-no-hit]"),
      confirm: document.querySelector("[data-department-picker-confirm]"),
    };
  }

  function toggle(popover, newState) {
    popover.dispatchEvent(Object.assign(new Event("toggle"), { newState }));
  }

  it("restores the selection when the popover closes without confirming", function () {
    const { popover, all, department } = render({ selected: ["Admins"] });

    toggle(popover, "open");
    department("Marketing").click();
    department("Admins").click();
    toggle(popover, "closed");

    expect(department("Admins").checked).toBe(true);
    expect(department("Marketing").checked).toBe(false);
    expect(all.checked).toBe(false);
  });

  it("keeps the selection when it is confirmed", function () {
    const { popover, department, confirm } = render({ selected: ["Admins"] });

    toggle(popover, "open");
    department("Marketing").click();
    confirm.click();
    toggle(popover, "closed");

    expect(department("Admins").checked).toBe(true);
    expect(department("Marketing").checked).toBe(true);
  });

  it("resets to the rendered selection when the page is restored from the back/forward cache", function () {
    const { popover, all, department, confirm } = render({ selected: ["Admins"] });

    toggle(popover, "open");
    department("Marketing").click();
    confirm.click();
    globalThis.dispatchEvent(Object.assign(new Event("pageshow"), { persisted: true }));

    expect(department("Admins").checked).toBe(true);
    expect(department("Marketing").checked).toBe(false);
    expect(all.checked).toBe(false);

    // the restored selection is staged again - closing without confirming keeps the rendered one
    department("Vertrieb").click();
    toggle(popover, "closed");

    expect(department("Vertrieb").checked).toBe(false);
  });

  it("clears the departments when all persons is ticked", function () {
    const { popover, all, department } = render({ selected: ["Admins", "Marketing"] });

    toggle(popover, "open");
    all.click();

    expect(all.checked).toBe(true);
    expect(department("Admins").checked).toBe(false);
    expect(department("Marketing").checked).toBe(false);
  });

  it("clears all persons when a department is ticked", function () {
    const { popover, all, department } = render({ all: true });

    toggle(popover, "open");
    department("Vertrieb").click();

    expect(all.checked).toBe(false);
    expect(department("Vertrieb").checked).toBe(true);
  });

  it("ticks all persons when the last department is unticked", function () {
    const { popover, all, department } = render({ selected: ["Admins"] });

    toggle(popover, "open");
    department("Admins").click();

    expect(department("Admins").checked).toBe(false);
    expect(all.checked).toBe(true);
  });

  it("keeps all persons ticked when it is unticked without a department", function () {
    const { popover, all } = render({ all: true });

    toggle(popover, "open");
    all.click();

    expect(all.checked).toBe(true);
  });

  it("does not let change events reach the host form", function () {
    const { form, popover, department } = render({ selected: ["Admins"] });
    const onChange = vi.fn();
    form.addEventListener("change", onChange);

    toggle(popover, "open");
    department("Marketing").click();

    expect(onChange).not.toHaveBeenCalled();
  });

  it("filters the departments by the search query", function () {
    const { popover, search, department, noHit } = render();

    toggle(popover, "open");
    search.value = "MARK";
    search.dispatchEvent(new Event("input", { bubbles: true }));

    expect(department("Marketing").closest("li").hidden).toBe(false);
    expect(department("Admins").closest("li").hidden).toBe(true);
    expect(department("Vertrieb").closest("li").hidden).toBe(true);
    expect(noHit.hidden).toBe(true);
  });

  it("shows the no-hit row when no department matches", function () {
    const { popover, search, noHit } = render();

    toggle(popover, "open");
    search.value = "xyz";
    search.dispatchEvent(new Event("input", { bubbles: true }));

    expect(noHit.hidden).toBe(false);
  });

  it("resets the search when the popover opens again", function () {
    const { popover, search, department, noHit } = render();

    toggle(popover, "open");
    search.value = "xyz";
    search.dispatchEvent(new Event("input", { bubbles: true }));
    toggle(popover, "closed");
    toggle(popover, "open");

    expect(search.value).toBe("");
    expect(department("Admins").closest("li").hidden).toBe(false);
    expect(noHit.hidden).toBe(true);
  });

  it("does not submit the form when enter is pressed in the search", function () {
    const { search } = render();
    const event = new KeyboardEvent("keydown", { key: "Enter", bubbles: true, cancelable: true });

    search.dispatchEvent(event);

    expect(event.defaultPrevented).toBe(true);
  });

  it("works without a search field", function () {
    document.body.innerHTML = `
      <uv-department-picker>
        <div popover>
          <input type="checkbox" name="allPersons" value="true" data-department-picker-all />
          <ul>
            <li><input type="checkbox" name="department" value="Admins" data-department-picker-department checked /></li>
            <li data-department-picker-no-hit hidden></li>
          </ul>
          <button type="submit" data-department-picker-confirm></button>
        </div>
      </uv-department-picker>
    `;
    const popover = document.querySelector("[popover]");

    expect(() => toggle(popover, "open")).not.toThrow();
  });
});
