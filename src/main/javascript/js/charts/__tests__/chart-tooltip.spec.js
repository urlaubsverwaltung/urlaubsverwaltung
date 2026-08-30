import { tooltipHtml, tooltipRowHtml } from "../chart-tooltip";

describe("tooltipHtml", () => {
  test("renders a title followed by the joined rows", () => {
    const html = tooltipHtml("absence-statistics", "March", ["<row-a>", "<row-b>"]);

    expect(html).toBe('<div class="absence-statistics-tooltip-title">March</div><row-a><row-b>');
  });

  test("renders a title with no rows", () => {
    const html = tooltipHtml("overtime-statistics", "March", []);

    expect(html).toBe('<div class="overtime-statistics-tooltip-title">March</div>');
  });
});

describe("tooltipRowHtml", () => {
  test("renders a colored swatch next to the given content", () => {
    const html = tooltipRowHtml("sicknote-statistics", "#ff0000", "Sick: 3 days");

    expect(html).toContain('class="sicknote-statistics-tooltip-row"');
    expect(html).toContain('class="sicknote-statistics-tooltip-swatch" style="background-color: #ff0000"');
    expect(html).toContain("Sick: 3 days");
  });
});
