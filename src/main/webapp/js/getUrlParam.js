// thanks to http://www.jaqe.de/2009/01/16/url-parameter-mit-javascript-auslesen/
export default function getUrlParam(name) {
  name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");

  var regexS = "[\\?&]" + name + "=([^&#]*)";
  var regex = new RegExp(regexS);
  var results = regex.exec(window.location.href);

  if (results == null)
    return "";
  else
    return results[1];
}
