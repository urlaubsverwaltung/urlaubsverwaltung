import { postJSON } from "../../js/fetch";

const feedbackForm = document.querySelector("#feedback-form");
const feedbackFormInputs = document.querySelector("#feedback-form-inputs");
const feedbackFormThankYou = document.querySelector("#feedback-form-thankyou");

feedbackForm.addEventListener('submit', async event => {
  event.preventDefault();

  const data = {
    type: feedbackForm.type.value,
    text: feedbackForm.text.value
  };

  feedbackFormInputs.style.display = 'none';
  feedbackFormThankYou.style.display = 'block';

  await postJSON('/api/feedback', data);
});
