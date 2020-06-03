export default function maxChars(element, max) {
  if (element.value.length > max) {
    element.value = element.value.slice(0, Math.max(0, max));
  }
}
