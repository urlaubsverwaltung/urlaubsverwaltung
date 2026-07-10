describe("absences-overview", function () {
  afterEach(function () {
    while (document.body.firstElementChild) {
      document.body.firstElementChild.remove();
    }
    vi.resetModules();
  });

  async function renderForm() {
    document.body.innerHTML = `<form id="absenceOverviewForm"></form>`;
    const form = document.querySelector("#absenceOverviewForm");
    form.submit = vi.fn();

    await import("../absences-overview");

    return form;
  }

  it("submits the form when it changes", async function () {
    const form = await renderForm();

    form.dispatchEvent(new Event("change", { bubbles: true }));

    expect(form.submit).toHaveBeenCalledTimes(1);
  });
});
