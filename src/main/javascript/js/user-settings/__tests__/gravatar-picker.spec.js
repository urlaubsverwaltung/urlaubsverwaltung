describe("gravatar-picker", function () {
  let userSettingsForm;
  let gravatarOn;
  let gravatarOff;

  beforeEach(async function () {
    vi.resetModules();

    document.body.innerHTML = `
      <form id="user-settings-form" action="/web/person/1/settings">
        <fieldset id="fieldset-gravatar">
          <input type="radio" name="gravatarEnabled" value="false" checked />
          <input type="radio" name="gravatarEnabled" value="true" />
        </fieldset>
        <input type="text" name="other" />
      </form>
    `;

    userSettingsForm = document.querySelector("#user-settings-form");
    gravatarOff = document.querySelector("input[value='false']");
    gravatarOn = document.querySelector("input[value='true']");
    userSettingsForm.submit = vi.fn();

    await import("../gravatar-picker");
  });

  function dispatchChange(element) {
    element.dispatchEvent(new Event("change", { bubbles: true }));
  }

  it("submits the form when the gravatar preference changes", function () {
    dispatchChange(gravatarOn);

    expect(userSettingsForm.submit).toHaveBeenCalledTimes(1);
  });

  it("submits the form when switching back to initials", function () {
    dispatchChange(gravatarOff);

    expect(userSettingsForm.submit).toHaveBeenCalledTimes(1);
  });

  it("does not submit the form when a non-gravatar field changes", function () {
    dispatchChange(document.querySelector("[name='other']"));

    expect(userSettingsForm.submit).not.toHaveBeenCalled();
  });
});
