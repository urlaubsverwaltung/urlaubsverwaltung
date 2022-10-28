const backButton = document.querySelector("[data-back-button]");
if (backButton) {
  backButton.addEventListener("click", () => {
    parent.history.back();
  });
}
