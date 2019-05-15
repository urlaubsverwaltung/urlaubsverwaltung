import $ from 'jquery';
import datepicker from "jquery-ui/ui/widgets/datepicker";

$(document).ready(async function () {

  const locale = window.navigator.language;
  if (locale === 'de') {
    const de = await import('jquery-ui/ui/i18n/datepicker-de');
    datepicker.setDefaults({
      ...de,
      weekHeader: 'Wo'
    });
  }
  else {
    const en = await import('jquery-ui/ui/i18n/datepicker-en-GB');
    datepicker.setDefaults({
      ...en,
      dateFormat: 'dd.mm.yy'
    });
  }

  $('#startDate').datepicker();
  $('#endDate').datepicker();
});
