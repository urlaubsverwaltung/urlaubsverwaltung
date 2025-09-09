const NONE = "-1";

function holidayReplacementChanged(value) {
  const holidayReplacementNoteRow = document.querySelector("#holidayReplacementNoteRow");

  holidayReplacementNoteRow.classList.toggle("hidden", value === NONE);
}

export default holidayReplacementChanged;
