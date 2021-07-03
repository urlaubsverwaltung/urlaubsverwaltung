import { post } from "../../js/fetch";

const selectElement = document.querySelector("#holiday-replacement-select");
const submitButton = selectElement.parentNode.parentNode.querySelector("button");

const formaction = submitButton.getAttribute("formaction");

selectElement.addEventListener("change", async function (event) {
  const response = await post(`${formaction}/replacements`, {
    body: new FormData(event.target.closest("form")),
    headers: {
      Accept: "text/html",
    },
  });

  if (response.ok) {
    const renderedHtml = await response.text();
    if (renderedHtml) {
      const template = document.createElement("template");
      template.innerHTML = renderedHtml;
      document.querySelector("#replacement-section-container ul").prepend(template.content);

      selectElement.querySelector('option[value="' + selectElement.value + '"]').remove();
      selectElement.blur();
    }
  }
});
