describe("settings-form", function () {
  beforeAll(async function () {
    await import("../settings-form");
  });

  afterEach(function () {
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
  });

  function domContentLoaded() {
    document.dispatchEvent(new Event("DOMContentLoaded", { bubbles: true, cancelable: true }));
  }

  function renderForm(selectedValue) {
    document.body.innerHTML = `
      <select id="calendarSettingsProvider">
        <option value="NoneSyncProvider" ${selectedValue === "NoneSyncProvider" ? "selected" : ""}>None</option>
        <option value="GoogleCalendarSyncProvider" ${
          selectedValue === "GoogleCalendarSyncProvider" ? "selected" : ""
        }>Google</option>
      </select>
      <div id="google-calendar"></div>
    `;
  }

  it("hides the google-calendar section when a different provider is selected", function () {
    renderForm("NoneSyncProvider");

    domContentLoaded();

    expect(document.querySelector("#google-calendar").hidden).toBe(true);
  });

  it("shows the google-calendar section when GoogleCalendarSyncProvider is selected", function () {
    renderForm("GoogleCalendarSyncProvider");

    domContentLoaded();

    expect(document.querySelector("#google-calendar").hidden).toBe(false);
  });

  it("toggles visibility again when the provider selection changes", function () {
    renderForm("NoneSyncProvider");
    domContentLoaded();
    const googleCalendar = document.querySelector("#google-calendar");
    expect(googleCalendar.hidden).toBe(true);

    const select = document.querySelector("#calendarSettingsProvider");
    select.value = "GoogleCalendarSyncProvider";
    select.dispatchEvent(new Event("change", { bubbles: true }));

    expect(googleCalendar.hidden).toBe(false);
  });

  it("does not throw when there is no provider select on the page", function () {
    document.body.innerHTML = `<div id="google-calendar"></div>`;

    expect(() => domContentLoaded()).not.toThrow();
    expect(document.querySelector("#google-calendar").hidden).toBe(true);
  });

  it("does not throw when there is no google-calendar section on the page", function () {
    document.body.innerHTML = `
      <select id="calendarSettingsProvider">
        <option value="GoogleCalendarSyncProvider" selected>Google</option>
      </select>
    `;

    expect(() => domContentLoaded()).not.toThrow();
  });

  it("does not throw when neither element exists", function () {
    document.body.innerHTML = ``;

    expect(() => domContentLoaded()).not.toThrow();
  });
});
