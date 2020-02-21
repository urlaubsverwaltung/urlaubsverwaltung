import $ from 'jquery'
import 'chosen-js';

(function() {

  /**
   * updates config section 'calendar sync'
   * shows dependent on Kalenderanbindung
   *   * Anbindung an Google Kalender
   *   * Anbindung an Microsoft Exchange Kalender
   */
  function updateVisibilityCalendar() {
    const calenderSettingsProvider = document.querySelector('#calendarSettingsProvider');
    const value = calenderSettingsProvider ? calenderSettingsProvider.value : '';

    const googleCalendar = document.querySelector('#google-calendar');
    if (googleCalendar) {
      googleCalendar.hidden = value !== 'GoogleCalendarSyncProvider';
    }

    const exchangeCalendar = document.querySelector('#exchange-calendar');
    if (exchangeCalendar) {
      exchangeCalendar.hidden = value !== 'ExchangeCalendarProvider';
    }

    if (value === 'ExchangeCalendarProvider') {
      // problem if div is not displayed
      // https://github.com/harvesthq/chosen/issues/92
      $(".chosenCombo").chosen({width: "100%"});
    } else {
      // if not visible deactivate
      $(".chosenCombo").chosen('destroy');
    }
  }

  $(document).ready(function () {
    // initial run to update view
    updateVisibilityCalendar();

    const calendarSettingsProvider = document.querySelector('#calendarSettingsProvider');
    if (calendarSettingsProvider) {
      calendarSettingsProvider.addEventListener('change', () => {
        updateVisibilityCalendar();
      })
    }
  });

})();
