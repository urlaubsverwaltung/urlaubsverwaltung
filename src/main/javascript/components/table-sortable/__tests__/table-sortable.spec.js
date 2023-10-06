import "..";

describe("table-sortable", () => {
  afterEach(function () {
    while (document.body.firstChild) {
      document.body.firstChild.remove();
    }

    jest.clearAllMocks();
  });

  it("is registered as a 'uv-table-sortable' custom element", function () {
    const element = customElements.get("uv-table-sortable");
    expect(element).toBeDefined();
  });

  it("sorts strings", function () {
    const div = document.createElement("div");
    div.innerHTML = `
      <table is="uv-table-sortable">
        <thead>
          <tr>
            <th data-sortable>Vorname</th>
            <th data-sortable>Nachname</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>Bruce</td>
            <td>Wayne</td>
          </tr>
          <tr>
            <td>Clark</td>
            <td>Kent</td>
          </tr>
        </tbody>
      </table>
    `;
    document.body.append(div);

    expect(text(document.querySelector("tbody tr:nth-child(1)"))).toEqual("Bruce Wayne");
    expect(text(document.querySelector("tbody tr:nth-child(2)"))).toEqual("Clark Kent");

    document.querySelector("thead th:nth-child(1)").click();
    expect(text(document.querySelector("tbody tr:nth-child(1)"))).toEqual("Bruce Wayne");
    expect(text(document.querySelector("tbody tr:nth-child(2)"))).toEqual("Clark Kent");

    document.querySelector("thead th:nth-child(1)").click();
    expect(text(document.querySelector("tbody tr:nth-child(1)"))).toEqual("Clark Kent");
    expect(text(document.querySelector("tbody tr:nth-child(2)"))).toEqual("Bruce Wayne");

    document.querySelector("thead th:nth-child(2)").click();
    expect(text(document.querySelector("tbody tr:nth-child(1)"))).toEqual("Clark Kent");
    expect(text(document.querySelector("tbody tr:nth-child(2)"))).toEqual("Bruce Wayne");

    document.querySelector("thead th:nth-child(2)").click();
    expect(text(document.querySelector("tbody tr:nth-child(1)"))).toEqual("Bruce Wayne");
    expect(text(document.querySelector("tbody tr:nth-child(2)"))).toEqual("Clark Kent");
  });

  it("sorts numeric", function () {
    const div = document.createElement("div");
    div.innerHTML = `
      <table is="uv-table-sortable">
        <thead>
          <tr>
            <th data-sortable data-sort-type="numeric">Number</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>5</td>
          </tr>
          <tr>
            <td>8</td>
          </tr>
          <tr>
            <td>4</td>
          </tr>
        </tbody>
      </table>
    `;
    document.body.append(div);

    expect(text(document.querySelector("tbody tr:nth-child(1)"))).toEqual("5");
    expect(text(document.querySelector("tbody tr:nth-child(2)"))).toEqual("8");
    expect(text(document.querySelector("tbody tr:nth-child(3)"))).toEqual("4");

    document.querySelector("thead th:nth-child(1)").click();
    expect(text(document.querySelector("tbody tr:nth-child(1)"))).toEqual("4");
    expect(text(document.querySelector("tbody tr:nth-child(2)"))).toEqual("5");
    expect(text(document.querySelector("tbody tr:nth-child(3)"))).toEqual("8");

    document.querySelector("thead th:nth-child(1)").click();
    expect(text(document.querySelector("tbody tr:nth-child(1)"))).toEqual("8");
    expect(text(document.querySelector("tbody tr:nth-child(2)"))).toEqual("5");
    expect(text(document.querySelector("tbody tr:nth-child(3)"))).toEqual("4");
  });

  it("sorts date", function () {
    const div = document.createElement("div");
    div.innerHTML = `
      <table is="uv-table-sortable">
        <thead>
          <tr>
            <th data-sortable data-sort-type="date">Datum</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>03.10.2023</td>
          </tr>
          <tr>
            <td>03.01.2023</td>
          </tr>
          <tr>
            <td>04.10.2023</td>
          </tr>
          <tr>
            <td>04.10.2022</td>
          </tr>
        </tbody>
      </table>
    `;
    document.body.append(div);

    expect(text(document.querySelector("tbody tr:nth-child(1)"))).toEqual("03.10.2023");
    expect(text(document.querySelector("tbody tr:nth-child(2)"))).toEqual("03.01.2023");
    expect(text(document.querySelector("tbody tr:nth-child(3)"))).toEqual("04.10.2023");
    expect(text(document.querySelector("tbody tr:nth-child(4)"))).toEqual("04.10.2022");

    document.querySelector("thead th").click();
    expect(text(document.querySelector("tbody tr:nth-child(1)"))).toEqual("04.10.2022");
    expect(text(document.querySelector("tbody tr:nth-child(2)"))).toEqual("03.01.2023");
    expect(text(document.querySelector("tbody tr:nth-child(3)"))).toEqual("03.10.2023");
    expect(text(document.querySelector("tbody tr:nth-child(4)"))).toEqual("04.10.2023");

    document.querySelector("thead th").click();
    expect(text(document.querySelector("tbody tr:nth-child(1)"))).toEqual("04.10.2023");
    expect(text(document.querySelector("tbody tr:nth-child(2)"))).toEqual("03.10.2023");
    expect(text(document.querySelector("tbody tr:nth-child(3)"))).toEqual("03.01.2023");
    expect(text(document.querySelector("tbody tr:nth-child(4)"))).toEqual("04.10.2022");
  });

  it("sorts if th is available instead of td", function () {
    const div = document.createElement("div");
    div.innerHTML = `
      <table is="uv-table-sortable">
        <thead>
          <tr>
            <th data-sortable data-sort-type="date">Datum</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <th>03.10.2023</th>
          </tr>
          <tr>
            <td>03.01.2023</td>
          </tr>
          <tr>
            <td>04.10.2023</td>
          </tr>
          <tr>
            <td>04.10.2022</td>
          </tr>
        </tbody>
      </table>
    `;
    document.body.append(div);

    expect(text(document.querySelector("tbody tr:nth-child(1)"))).toEqual("03.10.2023");
    expect(text(document.querySelector("tbody tr:nth-child(2)"))).toEqual("03.01.2023");
    expect(text(document.querySelector("tbody tr:nth-child(3)"))).toEqual("04.10.2023");
    expect(text(document.querySelector("tbody tr:nth-child(4)"))).toEqual("04.10.2022");

    document.querySelector("thead th").click();
    expect(text(document.querySelector("tbody tr:nth-child(1)"))).toEqual("04.10.2022");
    expect(text(document.querySelector("tbody tr:nth-child(2)"))).toEqual("03.01.2023");
    expect(text(document.querySelector("tbody tr:nth-child(3)"))).toEqual("03.10.2023");
    expect(text(document.querySelector("tbody tr:nth-child(4)"))).toEqual("04.10.2023");

    document.querySelector("thead th").click();
    expect(text(document.querySelector("tbody tr:nth-child(1)"))).toEqual("04.10.2023");
    expect(text(document.querySelector("tbody tr:nth-child(2)"))).toEqual("03.10.2023");
    expect(text(document.querySelector("tbody tr:nth-child(3)"))).toEqual("03.01.2023");
    expect(text(document.querySelector("tbody tr:nth-child(4)"))).toEqual("04.10.2022");
  });

  it("does not sort columns without 'data-sortable'", function () {
    const div = document.createElement("div");
    div.innerHTML = `
      <table is="uv-table-sortable">
        <thead>
          <tr>
            <th data-sortable>Vorname</th>
            <th>Nachname</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>Bruce</td>
            <td>Wayne</td>
          </tr>
          <tr>
            <td>Clark</td>
            <td>Kent</td>
          </tr>
        </tbody>
      </table>
    `;
    document.body.append(div);

    expect(text(document.querySelector("tbody tr:nth-child(1)"))).toEqual("Bruce Wayne");
    expect(text(document.querySelector("tbody tr:nth-child(2)"))).toEqual("Clark Kent");

    document.querySelector("thead th:nth-child(2)").click();
    expect(text(document.querySelector("tbody tr:nth-child(1)"))).toEqual("Bruce Wayne");
    expect(text(document.querySelector("tbody tr:nth-child(2)"))).toEqual("Clark Kent");
  });

  function text(element) {
    return element.textContent.trim().replaceAll(/\s+/g, " ");
  }
});
