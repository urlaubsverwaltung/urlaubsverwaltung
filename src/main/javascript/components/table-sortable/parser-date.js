export const dateParser = {
  id: "date-parser",
  is: function () {
    return false;
  },
  format: function (s, table, cell) {
    return new Date(cell.dataset.value).getTime();
  },
  parsed: false,
  type: "numeric",
};
