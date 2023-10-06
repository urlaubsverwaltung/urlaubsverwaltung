function initTableSortable(table) {
  for (let th of table.querySelectorAll("th[data-sortable]")) {
    const container = document.createElement("div");
    container.innerHTML = th.innerHTML;
    container.classList.add("tablesorter-header-inner");
    th.innerHTML = container.outerHTML;
  }

  let tbody = table.querySelector("tbody"); // we only have tables with one tbody. there is no list to consider.
  let currentSortedColumn;

  const ths = table.querySelectorAll("thead > tr > th");

  for (const [index, th] of ths.entries()) {
    const sortType = th.dataset.sortType ?? "string";
    const comparator = getComparator(sortType);

    th.addEventListener("click", function handleThClick(event) {
      if (!event.target.closest("th").matches("[data-sortable]")) {
        return;
      }

      const trs = [...tbody.querySelectorAll("tr")];
      const rows = trs.map((tr) => {
        const cell = tr.children[index];
        return [tr, getSortValue(cell, sortType)];
      });

      const sorted = Boolean(th.dataset.sorted === "true");
      const nextDirection = sorted ? (th.dataset.sortDirection === "asc" ? "desc" : "asc") : "asc";

      if (currentSortedColumn === index) {
        rows.reverse();
      } else {
        const desc = nextDirection === "desc";
        rows.sort((a, b) => {
          let valueA = a[1];
          let valueB = b[1];
          return desc ? comparator(valueB, valueA) : comparator(valueA, valueB);
        });
      }

      tbody.append(...rows.map((row) => row[0]));

      currentSortedColumn = index;
      th.dataset.sorted = "true";
      th.dataset.sortDirection = nextDirection;

      for (let _th of table.querySelectorAll("th[data-sortable]")) {
        if (_th !== th) {
          delete _th.dataset.sorted;
        }
      }
    });
  }
}

function getComparator(sortType) {
  if (sortType === "date") {
    return (a, b) => a.getTime() - b.getTime();
  }
  if (sortType === "numeric") {
    return (a, b) => a - b;
  }
  return (a, b) => a.localeCompare(b);
}

function getSortValue(cell, sortType) {
  if (!("sortValue" in cell.dataset)) {
    let sortValue = cell.textContent?.trim() ?? "";
    cell.dataset.sortValue = sortValue.replaceAll(/\s+/g, " ");
  }
  const value = cell.dataset.sortValue;
  if (sortType === "date") {
    return new Date(value);
  }
  if (sortType === "numeric") {
    return Number.parseFloat(value.replaceAll(",", "."));
  }
  return value;
}

export class TableSortable extends HTMLTableElement {
  connectedCallback() {
    initTableSortable(this);
  }
}

customElements.define("uv-table-sortable", TableSortable, { extends: "table" });
