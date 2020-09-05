import tooltip from '../tooltip'

class CopyToClipboardInputElement extends HTMLDivElement {
  connectedCallback() {
    const icon = document.createElement('i');
    icon.classList.add('fa', 'fa-copy', 'tw-text-xl');

    const button = document.createElement('button');
    button.classList.add('btn', 'btn-default', 'tw-m-0', 'tw-border-0', 'tw-outline-none');
    button.dataset.title = this.dataset.messageButtonTitle;
    button.dataset.placement = "bottom";
    button.append(icon);

    const input = this.querySelector('input');
    input.setAttribute('tabindex', '-1');

    button.addEventListener('click', async (event) => {
      event.preventDefault();
      event.stopPropagation();
      await navigator.clipboard.writeText(input.value);
      button.blur();
    });

    button.addEventListener('focus', function handleFocus() {
      selectWholeTextOfInputElement(input);
    });

    input.addEventListener('focus', function handleFocus() {
      selectWholeTextOfInputElement(input);
    });

    this.insertBefore(button, input.nextElementSibling);

    // initialize bootstrap tooltip widget
    tooltip();
  }
}

function selectWholeTextOfInputElement (inputElement) {
  inputElement.setSelectionRange(0, inputElement.value.length);
}

customElements.define('uv-copy-to-clipboard-input', CopyToClipboardInputElement, { extends: 'div' });
