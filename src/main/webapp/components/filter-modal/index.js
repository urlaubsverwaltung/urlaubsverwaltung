import $ from 'jquery';
import datepicker from "jquery-ui/ui/widgets/datepicker";

$(document).ready(async function () {

  const locale = window.navigator.language;
  switch(locale) {
    case 'de': {
      const de = await import('jquery-ui/ui/i18n/datepicker-de');
      datepicker.setDefaults({
        ...de,
        weekHeader: 'Wo'
      });
      break;
    }

    default: {
      const en = await import('jquery-ui/ui/i18n/datepicker-en-GB');
      datepicker.setDefaults({
        ...en,
        dateFormat: 'dd.mm.yy'
      });
      break;
    }
  }

  $('#startDate').datepicker();
  $('#endDate').datepicker();
});
