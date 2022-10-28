// thanks to http://www.jaqe.de/2009/01/16/url-parameter-mit-javascript-auslesen/
export default function getUrlParameter(name) {
  // eslint-disable-next-line no-useless-escape
  name = name.replace(/\[/, "\\[").replace(/]/, "\\]");

  const regexS = "[\\?&]" + name + "=([^&#]*)";
  const regex = new RegExp(regexS);
  const results = regex.exec(window.location.href);

  return results ? results[1] : "";
}
