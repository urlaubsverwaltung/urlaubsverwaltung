// script to count number of chars in textarea
export default function count(value, id) {
  document.querySelector('#' + id).innerHTML = value.length + "/";
}
