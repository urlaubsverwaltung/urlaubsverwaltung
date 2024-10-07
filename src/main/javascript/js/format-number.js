export default function formatNumber(number) {
  const locale = globalThis.navigator.language || "de";
  return Number(number).toLocaleString(locale, {
    maximumFractionDigits: 1,
    minimumFractionDigits: 0,
  });
}
