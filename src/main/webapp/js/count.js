// script to count number of chars in textarea
export default function count(val, id) {
  document.getElementById(id).innerHTML = val.length + "/";
}
