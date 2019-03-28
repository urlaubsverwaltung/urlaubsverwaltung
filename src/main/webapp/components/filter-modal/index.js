import $ from 'jquery';
import datepicker from "jquery-ui/ui/widgets/datepicker";

$(document).ready(async function () {

  const locale = window.navigator.language;
  switch(locale) {
    case 'de':
      const de = await import('jquery-ui/ui/i18n/datepicker-de');
      de.weekHeader = 'Wo';
      datepicker.setDefaults(de);
      break;

    default:
      const en = await import('jquery-ui/ui/i18n/datepicker-en-GB');
      en.dateFormat = 'dd.mm.yy';
      datepicker.setDefaults(en);
      break;
  }

  $('#startDate').datepicker();
  $('#endDate').datepicker();
});
