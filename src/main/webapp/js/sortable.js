$(function () {

  $.tablesorter.addParser({
    id: 'germanDate',
    is: function (s) {
      return false;
    },
    format: function (s) {
      var d;
      if (s.length > 10) {
        d = s.substring(0, 10);
      } else if (s.length == 10) {
        d = s;
      } else {
        return -1;
      }

      var a = d.split('.');
      a[1] = a[1].replace(/^[0]+/g, "");
      return new Date(a.reverse().join("/")).getTime();
    },
    type: 'numeric'
  });

  $.tablesorter.addParser({
    id: 'commaNumber',
    is: function (s) {
      return false;
    },
    format: function (s) {

      var reg = new RegExp("[0-9]+");

      if (reg.test(s)) {
        s = s.replace(/[\,\.]/g, '.');

        // possible that string is sth like that: 30 + 2
        return eval(s);
      } else {
        return -1;
      }

    },
    type: 'numeric'
  });

});

