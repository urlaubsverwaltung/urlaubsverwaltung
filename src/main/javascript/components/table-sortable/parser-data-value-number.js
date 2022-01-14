export const dataValueNumberParser = {
  id: "data-value-number",
  is: function () {
    return false;
  },
  format: function (s, table, cell) {
    const sortableValue = cell.dataset.sortableValue;
    if (sortableValue === "") {
      return Number.MIN_SAFE_INTEGER;
    }
    return Number(sortableValue);
  },
  parsed: false,
  type: "numeric",
};
