const backButton = document.querySelector('.btn.back');
if (backButton) {
  backButton.addEventListener('click', () => {
    parent.history.back();
  });
}
