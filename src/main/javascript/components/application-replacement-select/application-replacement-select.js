import { post } from "../../js/fetch";

const spinner = `
  <svg class="animate-spin transition-all h-4 w-0 text-black" fill="none" viewBox="0 0 24 24" width="16px" height="16px" xmlns="http://www.w3.org/2000/svg">
    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
  </svg>
`;

export function initApplicationReplacementSelect() {
  const selectElement = document.querySelector("#holiday-replacement-select");
  const submitButton = selectElement.parentNode.parentNode.querySelector("button");
  const replacementListElement = document.querySelector("#replacement-section-container ul");

  const listId = "added-replacements-list-element";
  replacementListElement.setAttribute("id", listId);

  selectElement.setAttribute("aria-controls", listId);
  replacementListElement.setAttribute("aria-live", "polite");
  replacementListElement.setAttribute("aria-relevant", "additions");

  const formaction = submitButton.getAttribute("formaction");

  const svg = htmlStringToNode(spinner);
  submitButton.prepend(svg);
  submitButton.classList.add("flex", "items-center");

  selectElement.addEventListener("change", async function (event) {
    submitButton.firstElementChild.classList.add("w-4", "mr-2");

    submitButton.addEventListener("click", preventDefault);
    submitButton.setAttribute("aria-disabled", "true");

    const [{ value: response }] = await Promise.allSettled([
      post(`${formaction}/replacements`, {
        body: new FormData(event.target.closest("form")),
        headers: {
          Accept: "text/html",
        },
      }),
      wait(300),
    ]);

    if (response.ok) {
      const renderedHtml = await response.text();
      if (renderedHtml) {
        const li = htmlStringToNode(renderedHtml);
        document.querySelector("#replacement-section-container ul").prepend(li);

        selectElement.querySelector('option[value="' + selectElement.value + '"]').remove();
        selectElement.blur();
      }
    }

    submitButton.firstElementChild.classList.remove("w-4", "mr-2");
    submitButton.removeAttribute("disabled");

    submitButton.removeEventListener("click", preventDefault);
    submitButton.removeAttribute("aria-disabled");
  });
}

function preventDefault(event) {
  event.preventDefault();
}

function wait(delay) {
  return new Promise(function (resolve) {
    setTimeout(resolve, delay);
  });
}

function htmlStringToNode(htmlText) {
  const template = document.createElement("template");
  template.innerHTML = htmlText;
  return template.content;
}
