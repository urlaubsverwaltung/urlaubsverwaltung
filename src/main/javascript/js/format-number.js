export default function formatNumber(number) {
  // the language of the page (th:lang), a german page on an english browser shows german numbers
  const locale = document.documentElement.lang || "de";
  return Number(number).toLocaleString(locale, {
    maximumFractionDigits: 1,
    minimumFractionDigits: 0,
  });
}
