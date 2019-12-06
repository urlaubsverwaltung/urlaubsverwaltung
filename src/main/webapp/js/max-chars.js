export default function maxChars(element, max) {
  if (element.value.length > max) {
    element.value = element.value.substring(0, max);
  }
}
