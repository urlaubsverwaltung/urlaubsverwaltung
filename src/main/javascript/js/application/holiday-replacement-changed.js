const NONE = "-1";

function holidayReplacementChanged(value) {
  const holidayReplacementNoteRow = document.querySelector("#holidayReplacementNoteRow");

  if (value === NONE) {
    holidayReplacementNoteRow.classList.add("hidden");
  } else {
    holidayReplacementNoteRow.classList.remove("hidden");
  }
}

export default holidayReplacementChanged;
