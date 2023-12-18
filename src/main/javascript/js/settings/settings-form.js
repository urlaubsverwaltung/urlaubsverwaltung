import $ from "jquery";

(function () {
  /**
   * updates config section 'calendar sync'
   * shows dependent on Kalenderanbindung
   *   * Anbindung an Google Kalender
   */
  function updateVisibilityCalendar() {
    const calendarSettingsProvider = document.querySelector("#calendarSettingsProvider");
    const value = calendarSettingsProvider ? calendarSettingsProvider.value : "";

    const googleCalendar = document.querySelector("#google-calendar");
    if (googleCalendar) {
      googleCalendar.hidden = value !== "GoogleCalendarSyncProvider";
    }
  }

  $(document).ready(function () {
    // initial run to update view
    updateVisibilityCalendar();

    const calendarSettingsProvider = document.querySelector("#calendarSettingsProvider");
    if (calendarSettingsProvider) {
      calendarSettingsProvider.addEventListener("change", () => {
        updateVisibilityCalendar();
      });
    }
  });
})();
