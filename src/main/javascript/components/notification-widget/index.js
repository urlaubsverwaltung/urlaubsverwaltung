const link = document.querySelector("#nav-notification-link");
const root = link.parentNode;

document.addEventListener("click", function (event) {
  if (event.target === link || event.target.closest("#nav-notification-link")) {
    event.preventDefault();
    showNotificationWidget();
  } else {
    // TODO clicking within the visible notification widget should not close it, maybe
    hideNotificationWidget();
  }
});

const widget = document.createElement("div");
widget.classList.add("tw-absolute", "tw-right-3"); // relative to the navbar
widget.innerHTML = `
  <div class="tw-space-y-4 tw-text-white tw-bg-gray-900 tw-bg-opacity-50 tw-p-4 tw-rounded-lg tw-w-64" style="backdrop-filter: blur(7px);">
    <div class="">
      <p class="">Lorem ipsum</p>
    </div>
    <div class="">
      <p class="">Lorem ipsum</p>
    </div>
    <div class="">
      <p class="">Lorem ipsum</p>
    </div>
    <div class="">
      <p class="">Lorem ipsum</p>
    </div>
  </div>
`;

function showNotificationWidget() {
  root.append(widget);
}

function hideNotificationWidget() {
  widget.remove();
}
