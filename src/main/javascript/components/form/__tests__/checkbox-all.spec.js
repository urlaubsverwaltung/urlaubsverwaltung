import "../checkbox-all";

describe("checkbox-all", function () {
  afterEach(function () {
    // cleanup DOM (also exercises disconnectedCallback for every uv-checkbox-all instance)
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
    vi.restoreAllMocks();
  });

  function renderForm(extraHtml = "") {
    document.body.innerHTML = `
      <form>
        <input is="uv-checkbox-all" type="checkbox" id="select-all" />
        ${extraHtml}
        <input type="checkbox" id="cb1" />
        <input type="checkbox" id="cb2" />
        <input type="checkbox" id="cb3" />
      </form>
    `;
  }

  function checkboxes(...ids) {
    return ids.map((id) => document.querySelector(`#${id}`));
  }

  it("checks all other checkboxes in the same form when checked", function () {
    renderForm();
    const [selectAll, cb1, cb2, cb3] = checkboxes("select-all", "cb1", "cb2", "cb3");

    selectAll.click();

    expect(cb1.checked).toBe(true);
    expect(cb2.checked).toBe(true);
    expect(cb3.checked).toBe(true);
  });

  it("unchecks all other checkboxes in the same form when unchecked", function () {
    renderForm();
    const [selectAll, cb1, cb2, cb3] = checkboxes("select-all", "cb1", "cb2", "cb3");

    selectAll.click(); // check all
    selectAll.click(); // uncheck all

    expect(cb1.checked).toBe(false);
    expect(cb2.checked).toBe(false);
    expect(cb3.checked).toBe(false);
  });

  it("dispatches a change event on every checkbox it toggles", function () {
    renderForm();
    const [selectAll, cb1, cb2, cb3] = checkboxes("select-all", "cb1", "cb2", "cb3");

    const changeHandler = vi.fn();
    for (const checkbox of [cb1, cb2, cb3]) {
      checkbox.addEventListener("change", changeHandler);
    }

    selectAll.click();

    expect(changeHandler).toHaveBeenCalledTimes(3);
  });

  it("becomes checked once every other checkbox in the form is checked", function () {
    renderForm();
    const [selectAll, cb1, cb2, cb3] = checkboxes("select-all", "cb1", "cb2", "cb3");

    cb1.click();
    cb2.click();
    expect(selectAll.checked).toBe(false);

    cb3.click();
    expect(selectAll.checked).toBe(true);
  });

  it("becomes unchecked once any other checkbox in the form is unchecked", function () {
    renderForm();
    const [selectAll, cb1, cb2, cb3] = checkboxes("select-all", "cb1", "cb2", "cb3");

    selectAll.click(); // check all, selectAll is now checked
    expect(selectAll.checked).toBe(true);

    cb2.click(); // uncheck one

    expect(selectAll.checked).toBe(false);
  });

  it("does not react to checkboxes outside of its own form", function () {
    renderForm();
    document.body.insertAdjacentHTML("beforeend", `<form><input type="checkbox" id="cb-other-form" /></form>`);
    const selectAll = document.querySelector("#select-all");
    const otherFormCheckbox = document.querySelector("#cb-other-form");

    otherFormCheckbox.click();

    expect(selectAll.checked).toBe(false);
  });

  describe("data-ignore", function () {
    it("does not cascade to the ignored checkbox when 'select all' is checked", function () {
      renderForm(`<input type="checkbox" id="ignored" />`);
      const selectAll = document.querySelector("#select-all");
      selectAll.dataset.ignore = "ignored";
      const ignored = document.querySelector("#ignored");
      const [cb1, cb2, cb3] = checkboxes("cb1", "cb2", "cb3");

      selectAll.click();

      expect(ignored.checked).toBe(false);
      expect(cb1.checked).toBe(true);
      expect(cb2.checked).toBe(true);
      expect(cb3.checked).toBe(true);
    });

    it("does not react at all when the ignored checkbox itself changes", function () {
      renderForm(`<input type="checkbox" id="ignored" />`);
      const selectAll = document.querySelector("#select-all");
      selectAll.dataset.ignore = "ignored";
      const ignored = document.querySelector("#ignored");
      const [cb1, cb2, cb3] = checkboxes("cb1", "cb2", "cb3");

      // check every other checkbox first, so "all checked" would be true if evaluated
      cb1.click();
      cb2.click();
      cb3.click();
      expect(selectAll.checked).toBe(false);

      ignored.click();

      expect(selectAll.checked).toBe(false);
    });

    it("still counts the ignored checkbox's state when determining 'all checked'", function () {
      renderForm(`<input type="checkbox" id="ignored" />`);
      const selectAll = document.querySelector("#select-all");
      selectAll.dataset.ignore = "ignored";
      const ignored = document.querySelector("#ignored");
      const [cb1, cb2, cb3] = checkboxes("cb1", "cb2", "cb3");

      cb1.click();
      cb2.click();
      cb3.click();
      expect(selectAll.checked).toBe(false); // ignored is still unchecked

      // set the ignored checkbox without going through its own (early-returning) change handling
      ignored.checked = true;
      cb3.click(); // uncheck ...
      cb3.click(); // ... and recheck to trigger the "all checked" evaluation again

      expect(selectAll.checked).toBe(true);
    });
  });

  it("does not throw when the element is removed from the DOM", function () {
    renderForm();

    expect(() => document.querySelector("form").remove()).not.toThrow();
  });

  it("removes its global change listener when disconnected, so it doesn't leak on reconnect", function () {
    renderForm();
    const form = document.querySelector("form");

    const addSpy = vi.spyOn(globalThis, "addEventListener");
    const removeSpy = vi.spyOn(globalThis, "removeEventListener");

    form.remove();
    expect(removeSpy).toHaveBeenCalledWith("change", expect.any(Function));

    document.body.append(form); // reconnect: re-registers exactly one fresh listener
    expect(addSpy).toHaveBeenCalledWith("change", expect.any(Function));

    const selectAll = document.querySelector("#select-all");
    const [cb1, cb2, cb3] = checkboxes("cb1", "cb2", "cb3");
    cb1.click();
    cb2.click();
    cb3.click();

    // only one active listener after remove+reconnect -> reacts exactly once per click
    expect(selectAll.checked).toBe(true);
  });
});
