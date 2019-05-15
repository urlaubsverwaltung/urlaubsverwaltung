const standardNumberOfRows = 1;
const expandedNumberOfRows = 4;

[...document.querySelectorAll("textarea")].forEach(textarea => {

  textarea.addEventListener('focus', () => {
    textarea.rows = expandedNumberOfRows;
  });

  textarea.addEventListener('blur', () => {
    if (textarea.value == '') {
      textarea.rows = standardNumberOfRows;
    }
  });
});
