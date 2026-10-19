import { escapeHtml, escapeHtmlContent } from "../escape-html";

describe("escapeHtml", () => {
  test("renders markup as text when inserted as html", () => {
    const text = `<img src="x" onerror='alert(1)'> & more`;

    const element = document.createElement("span");
    element.innerHTML = escapeHtml(text);

    expect(element.textContent).toBe(text);
    expect(element.children).toHaveLength(0);
  });

  test("keeps text without markup as it is", () => {
    expect(escapeHtml("Erholungsurlaub")).toBe("Erholungsurlaub");
  });

  test("escapes quotes so the text cannot leave an attribute value", () => {
    expect(escapeHtml(`"'`)).toBe("&quot;&#39;");
  });
});

describe("escapeHtmlContent", () => {
  test("renders markup as text when inserted as element content", () => {
    const text = `<img src="x" onerror='alert(1)'> & <b>more</b>`;

    const element = document.createElement("span");
    element.innerHTML = escapeHtmlContent(text);

    expect(element.textContent).toBe(text);
    expect(element.children).toHaveLength(0);
  });

  test("keeps ampersands, quotes and closing brackets readable", () => {
    expect(escapeHtmlContent(`Kur & "Reha" > 'Urlaub'`)).toBe(`Kur & "Reha" > 'Urlaub'`);
  });
});
