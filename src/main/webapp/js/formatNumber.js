export default function formatNumber(number) {

  return new Number(number).toLocaleString("de", {
    maximumFractionDigits: 1,
    minimumFractionDigits: 0
  });
}
