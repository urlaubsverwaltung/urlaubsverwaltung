export default function maxChars(elem, max) {
  if (elem.value.length > max) {
    elem.value = elem.value.substring(0, max);
  }
}
