import tooltip from "../tooltip";

const icons = {
  clipboardCopy: `<svg fill="none" viewBox="0 0 24 24" stroke="currentColor" width="16px" height="16px" class="tw-w-4 tw-h-4 tw-stroke-2" role="img" aria-hidden="true" focusable="false"><path stroke-linecap="round" stroke-linejoin="round" d="M8 5H6a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2v-1M8 5a2 2 0 002 2h2a2 2 0 002-2M8 5a2 2 0 012-2h2a2 2 0 012 2m0 0h2a2 2 0 012 2v3m2 4H10m0 0l3-3m-3 3l3 3"></path></svg>`,
};

class CopyToClipboardInputElement extends HTMLDivElement {
  connectedCallback() {
    const button = document.createElement("button");
    button.classList.add("button", "tw-m-0", "tw-border-0", "tw-outline-none", "button--no-hover");
    button.dataset.title = this.dataset.messageButtonTitle;
    button.dataset.placement = "bottom";
    button.innerHTML = icons.clipboardCopy;

    const input = this.querySelector("input");
    input.setAttribute("tabindex", "-1");

    button.addEventListener("click", async (event) => {
      event.preventDefault();
      event.stopPropagation();
      await navigator.clipboard.writeText(input.value);
      button.blur();
    });

    button.addEventListener("focus", function handleFocus() {
      selectWholeTextOfInputElement(input);
    });

    input.addEventListener("focus", function handleFocus() {
      selectWholeTextOfInputElement(input);
    });

    this.insertBefore(button, input.nextElementSibling);

    // initialize bootstrap tooltip widget
    tooltip();
  }
}

function selectWholeTextOfInputElement(inputElement) {
  inputElement.setSelectionRange(0, inputElement.value.length);
}

customElements.define("uv-copy-to-clipboard-input", CopyToClipboardInputElement, { extends: "div" });
